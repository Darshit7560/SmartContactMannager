package com.project.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.entities.User;
import com.project.helper.Message;
import com.project.repo.UserRepository;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	
	@RequestMapping("/")
	public String home(Model model)
	{
		model.addAttribute("title", "home-smart contact manager");
		return "home";
	}
	
	
	@RequestMapping("/about")
	public String about(Model model)
	{
		model.addAttribute("title", "about-smart contact manager");
		return "about";
	}
	
	@RequestMapping("/signup")
	public String signup(Model model)
	{
		model.addAttribute("title", "register-smart contact manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	
	//this handeler for register user
	
	@PostMapping("/do_register")
	public String  registerUser(@Valid @ModelAttribute("user") User user,@RequestParam(value="agreement" ,defaultValue = "false") boolean agreement ,Model model, HttpSession session)
	{
		
		try
		{
			if(!agreement)
			{
				System.out.println("You have not agreed the terms and conditionn");
				throw new Exception("You have not agreed the terms and conditionn");
			}
			/*
			if(result1.hasErrors())
			{
				System.out.println("ERRO"+result1.toString());
				model.addAttribute("user" ,user);
				
				return "signup";
			}
			*/
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement"+agreement);
			System.out.println("USER"+user);
			
			User result = this.userRepository.save(user);
			
			model.addAttribute("user",new User());
			session.setAttribute("message", new Message("Successfully Register!!","alert-success"));
			return "signup";
		}
		catch(Exception e)
		{
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("something went wrong!!"+e.getMessage(),"alert-danger"));
			return "signup";
		}
		
	}
	
	@GetMapping("/signin")
	public String customeLogin(Model model)
	{
		
		model.addAttribute("title", "login page");
		return "login";
	}

}
