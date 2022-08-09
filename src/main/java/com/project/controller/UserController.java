package com.project.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.project.entities.Contact;
import com.project.entities.User;
import com.project.helper.Message;
import com.project.repo.ContactRepository;
import com.project.repo.UserRepository;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	// method for adding commom data to response
	@ModelAttribute
	public void addCommomData(Model m, Principal principal) {
		String username = principal.getName();
		System.out.println(username);

		User user = userRepository.getUserBYUserName(username);
		System.out.println("USER" + user);

		m.addAttribute("user", user);
	}

	// dashebord home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal)

	{
		model.addAttribute("title", "User DashBord");

		// get the user using username
		return "normal/user_dashbord";
	}

	// open add form handler
	@GetMapping("/add-contact")
	public String openAddCintactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	// processing add contact form

	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {

		try {
			String name = principal.getName();
			User user = this.userRepository.getUserBYUserName(name);

			// processing and uplodinig image
			/*
			 * if(3>2) { throw new Exception(); }
			 */

			if (file.isEmpty()) {
				// if file is empty then try message
				System.out.println("file is  not uploded");
				contact.setImage("contact.png");

			} else {
				// file the file to folder and update the name to catact
				contact.setImage(file.getOriginalFilename());
				File savefile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("image is uploded");
			}

			contact.setUser(user);
			
			user.getContacts().add(contact);

			this.userRepository.save(user);

			System.out.println("Added to data base");

			System.out.println("DATA" + contact);

			// message success.........

			session.setAttribute("message", new Message("your contact is added !! add more", "success"));

		} catch (Exception e) {
			System.out.println("ERROR" + e.getMessage());
			e.printStackTrace();

			// message unssucess
			session.setAttribute("message", new Message("something went wrong", "danger"));

		}
		return "normal/add_contact_form";
	}

	// show contacts handler
	// per page =5[n]
	// current page =0[page]

	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {
		m.addAttribute("title", "Show User Contacts");

		// contact ki list bhejani hai
		/*
		 * String userName=principal.getName(); User user =
		 * this.userRepository.getUserBYUserName(userName); 
		 * List<Contact> contacts = user.getContacts();
		 */

		String name = principal.getName();
		User user = this.userRepository.getUserBYUserName(name);

		// currentpage-page
		// contact per page-5
		PageRequest pageable = PageRequest.of(page, 5);
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		return "normal/show_contacts";
	}
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {

		System.out.println("CID" + cId);

		Optional<Contact> contactoptional = this.contactRepository.findById(cId);
		Contact contact = contactoptional.get();

		String userName = principal.getName();
		User user = this.userRepository.getUserBYUserName(userName);
		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
		}

		return "normal/contact_detail";
	}

	// delete contact handler
	@RequestMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model model, HttpSession session ,Principal principal) {

		Optional<Contact> contactoptional = this.contactRepository.findById(cId);
		Contact contact = contactoptional.get();

		// check.. not deleting contact so set as a null and unlink to the user beacuse
		// of we do not change in user casced type all so
		//contact.setUser(null);

		//this.contactRepository.delete(contact);
		
		User user=this.userRepository.getUserBYUserName(principal.getName());
		user.getContacts().remove(contact);
		
		this.userRepository.save(user);

		session.setAttribute("message", new Message("contact deleted successfully", "success"));

		return "redirect:/user/show-contacts/0";
	}
	
	//open update form handller
	
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId , Model m)
	{
		
		m.addAttribute("title","update contact");
	Contact contact = this.contactRepository.findById(cId).get();
	m.addAttribute("contact",contact);
		return "normal/update_from";
	}
	
	
	//update contact handler
	
	@RequestMapping(value="/process-update", method=RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact , @RequestParam("profileImage") MultipartFile file , Model model, HttpSession session , Principal principal)
	{
		try
		{
			//old contact details
			Contact oldcontactDetail = this.contactRepository.findById(contact.getcId()).get();
			
			if(!file.isEmpty())
			{
				//file work
				//rewrite
				
				//delete old photo
				File deletefile = new ClassPathResource("static/img").getFile();
				File file1=new File(deletefile , oldcontactDetail.getImage());
				
				//update new photo
				
				File savefile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
			}
			else
			{
				contact.setImage(oldcontactDetail.getImage());
			}
			User user=this.userRepository.getUserBYUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("your contact is updated......", "success"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("Contact name"+contact.getName());
		System.out.println("Contact is"+contact.getcId());
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
   //your profile handller
	
	@GetMapping("/profile")
	public String yourProfile(Model model)
	{
		model.addAttribute("title", "profile page");
		
		
		return "normal/profile";
	}

}
