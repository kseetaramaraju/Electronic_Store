package com.electronics_store.serviceImpl;


import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.electronics_store.cache.CacheBeanConfig;
import com.electronics_store.cache.CacheStore;
import com.electronics_store.entity.AccessToken;
import com.electronics_store.entity.Customer;
import com.electronics_store.entity.RefreshToken;
import com.electronics_store.entity.Seller;
import com.electronics_store.entity.User;
import com.electronics_store.enums.UserRole;
import com.electronics_store.exception.EmailAlreadyExist;
import com.electronics_store.exception.IllegelUserRoleException;
import com.electronics_store.exception.InvalidOTPException;
import com.electronics_store.exception.OtpExpiredException;
import com.electronics_store.exception.RegistrationSessionExpiredException;
import com.electronics_store.repository.AccessTokenRepo;
import com.electronics_store.repository.CustomerRepository;
import com.electronics_store.repository.RefreshTokenRepo;
import com.electronics_store.repository.SellerRepository;
import com.electronics_store.repository.UserRepository;
import com.electronics_store.requestdto.AuthRequest;
import com.electronics_store.requestdto.OtpModel;
import com.electronics_store.requestdto.UserRequest;
import com.electronics_store.responsedto.AuthResponse;
import com.electronics_store.responsedto.UserResponse;
import com.electronics_store.security.JwtService;
import com.electronics_store.service.AuthService;
import com.electronics_store.util.CookieManager;
import com.electronics_store.util.MessageStructure;
import com.electronics_store.util.ResponseEntityProxy;
import com.electronics_store.util.ResponseStructure;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService{

	private UserRepository userRepo;
	private CustomerRepository customerRepo;
	private SellerRepository sellerRepo;
	private ResponseStructure<UserResponse> structure;
	private ResponseStructure<AuthResponse> authstructure;
	private PasswordEncoder passwordEncoder;
	private CacheStore<String> otpcachestore;
	private CacheStore<User> usercachestore;
	private JavaMailSender javaMailSender;
	private AuthenticationManager authenticationManager;
	private CookieManager cookieManager;
	private JwtService jwtService;
	private AccessTokenRepo accessTokenRepo;
	private RefreshTokenRepo refreshTokenRepo;

	@Value("${myapp.access.expiry}")
	private long accessExpiryInSeconds;

	@Value("${myapp.refresh.expiry}")
	private long refreshExpiryInSeconds;

	public AuthServiceImpl(UserRepository userRepo, CustomerRepository customerRepo, SellerRepository sellerRepo,
			ResponseStructure<UserResponse> structure,ResponseStructure<AuthResponse> authstructure ,PasswordEncoder passwordEncoder,
			CacheStore<String> otpcachestore, CacheStore<User> usercachestore, JavaMailSender javaMailSender,
			AuthenticationManager authenticationManager, CookieManager cookieManager, JwtService jwtService,
			AccessTokenRepo accessTokenRepo, RefreshTokenRepo refreshTokenRepo) {
		super();
		this.userRepo = userRepo;
		this.customerRepo = customerRepo;
		this.sellerRepo = sellerRepo;
		this.structure = structure;
		this.authstructure=authstructure;
		this.passwordEncoder = passwordEncoder;
		this.otpcachestore = otpcachestore;
		this.usercachestore = usercachestore;
		this.javaMailSender = javaMailSender;
		this.authenticationManager = authenticationManager;
		this.cookieManager = cookieManager;
		this.jwtService = jwtService;
		this.accessTokenRepo = accessTokenRepo;
		this.refreshTokenRepo = refreshTokenRepo;
	}

	public <T extends User> T mapToRespective(UserRequest userRequest)
	{
		User user=null;

		switch ( UserRole.valueOf(userRequest.getUserRole().toUpperCase()) ) {
		case SELLER -> {user = new Seller();}
		case CUSTOMER -> {user = new Customer();}
		}

		user.setUserName(userRequest.getUserEmail().split("@")[0].toString());
		user.setUserEmail(userRequest.getUserEmail());
		user.setUserPassword(passwordEncoder.encode(userRequest.getUserPassword()));
		user.setUserRole(UserRole.valueOf(userRequest.getUserRole().toUpperCase()));
		user.setDeleted(false);
		user.setEmailVerified(false);

		return (T)user;
	}

	public UserResponse mapToUserResponse(User user)
	{
		return UserResponse.builder()
				.userId(user.getUserId())
				.userName(user.getUserName())
				.userEmail(user.getUserEmail())
				.userRole(user.getUserRole())
				.isDeleted(user.isDeleted())
				.isEmailVerified(user.isEmailVerified())
				.build();
	}
	
	public AuthResponse mapToAuthResponse(User user)
	{
		return AuthResponse.builder()
				.userId(user.getUserId())
				.userName(user.getUserName())
				.userRole(user.getUserRole().name())
				.isAthenticated(true)
				.accessExpiration( LocalDateTime.now().plusSeconds(accessExpiryInSeconds))
				.refreshExpiration(LocalDateTime.now().plusSeconds(refreshExpiryInSeconds))
				.build();
	}






	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> register( UserRequest userRequest) throws MessagingException {


		if(userRepo.existsByUserEmail(userRequest.getUserEmail()))
		{
			throw new EmailAlreadyExist("User with given Email is Already Exist!!");
		}

		String otp=generateOTP();
		User user=mapToRespective(userRequest);
		usercachestore.add(userRequest.getUserEmail(), user);
		otpcachestore.add(userRequest.getUserEmail(), otp);

		try {
			sendOtpToMail(user, otp);
		} catch (MessagingException e) {

			log.error(" The Email Address Does Not Exist!! ");
		}

		if( userRepo.existsById(user.getUserId()))
		{
			confirmMail(user);
		}

		return ResponseEntityProxy.setResponseStructure(HttpStatus.ACCEPTED,
				"Please Verify through OTP send on Email Id"
				,mapToUserResponse(user));

	}


	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> verifyOTP(OtpModel otpmodel) {

		User user = usercachestore.get(otpmodel.getEmail());
		String otp = otpcachestore.get(otpmodel.getEmail());


		if(otp==null) throw new OtpExpiredException("OTP expired!!");
		if(user==null) throw new RegistrationSessionExpiredException("Registration Session Expired Please Try After 24 Hours");
		if(!otp.equals(otpmodel.getOtp())) throw new InvalidOTPException("Invalid OTP!!"); 

		user.setEmailVerified(true);
		userRepo.save(user);

		return ResponseEntityProxy.setResponseStructure(HttpStatus.CREATED,
				"Otp Verified Successfully And User Saved To Database Successfully!!"
				,mapToUserResponse(user));

	}



	@Override
	public ResponseEntity<ResponseStructure<AuthResponse>> login(AuthRequest authRequest,HttpServletResponse response) {

		String username=authRequest.getUserEmail().split("@")[0];

		UsernamePasswordAuthenticationToken token =new UsernamePasswordAuthenticationToken(username,authRequest.getUserPassword());

		Authentication authenticate = authenticationManager.authenticate(token);

		if(!authenticate.isAuthenticated())
		{
			throw new UsernameNotFoundException("Failed To Authenticate The User!!");
		}
		else
		{
           return userRepo.findByUserName(username).map(user ->{
        	   
        	   grandAccess(response, user);
        	   
        	   return ResponseEntityProxy.setResponseStructure(HttpStatus.OK,"Login Done Successfully!!", 
        			   mapToAuthResponse(user));
        	   
           }).get();
		}

	}


	//-------------------------------------------------------------------------------------------------------------------------------------------------

	private void grandAccess(HttpServletResponse response,User user)
	{
		//generating access and refresh tokens
		String accessToken = jwtService.generateAccessToken(user.getUserName());
		String refreshToken = jwtService.generateRefreshToken(user.getUserName());

		//adding the access and refresh token cookies to the response
		response.addCookie(cookieManager.configure(new Cookie("at",accessToken),accessExpiryInSeconds)); 
		response.addCookie(cookieManager.configure(new Cookie("rt",refreshToken),refreshExpiryInSeconds));

		//Saving the access and refresh tokens in the database
		accessTokenRepo.save(AccessToken.builder()
				.token(accessToken)
				.isBlocked(false)
				.expiration(LocalDateTime.now().plusSeconds(accessExpiryInSeconds))
				.build());

		refreshTokenRepo.save(RefreshToken.builder()
				.token(refreshToken)
				.isBlocked(false)
				.expiration(LocalDateTime.now().plusSeconds(refreshExpiryInSeconds))
				.build());




	}

	private User saveUser(UserRequest userRequest)
	{
		User user=null;

		switch ( UserRole.valueOf(userRequest.getUserRole().toUpperCase()) ) {

		case SELLER -> {user=sellerRepo.save(mapToRespective(userRequest));}
		case CUSTOMER -> {user=customerRepo.save(mapToRespective(userRequest));}
		default-> throw new IllegelUserRoleException("Illegal UserRole!!");
		}
		return user;

	}

	@Async
	private void sendMail(MessageStructure message) throws MessagingException
	{
		MimeMessage mimemessage = javaMailSender.createMimeMessage();
		MimeMessageHelper helper=new MimeMessageHelper(mimemessage,true);

		helper.setTo(message.getTo());
		helper.setSubject(message.getSubject());
		helper.setSentDate(message.getSendDate());
		helper.setText(message.getText(),true);

		javaMailSender.send(mimemessage);

	}

	private void sendOtpToMail(User user,String otp) throws MessagingException
	{
		sendMail( MessageStructure.builder()
				.to(user.getUserEmail())
				.subject("Complete Your Registration Process!!")
				.sendDate(new Date())
				.text(  "Hey !"+ user.getUserName() +"Good To See you Intrested in Electronics_Store"
						+"Complete your Registration using the OTP <br> "
						+"<h1> "+otp+"</h1> <br>"
						+"Note : the Otp expires in 1 minute"
						+"<br> <br>"
						+"with best Regards!!<br>"
						+"Electronics_Store!!"
						)
				.build());
	}

	private void confirmMail(User user) throws MessagingException
	{
		sendMail( MessageStructure.builder()
				.to(user.getUserEmail())
				.subject("Completed Your Registration Process Sucessfully!!")
				.sendDate(new Date())
				.text(  "Hey Namaste Guru ! "+ user.getUserName() +" Welcome to Electronics_Store Enjoy pandagooo!!"

						)
				.build());
	}


	private String generateOTP()
	{
		return String.valueOf(new Random().nextInt(100000,999999));
	}



}
