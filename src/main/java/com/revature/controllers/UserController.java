package com.revature.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.revature.models.LoginTemplate;
import com.revature.models.User;
import com.revature.services.UserService;
import com.revature.services.UserServiceImpl;

import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import jakarta.servlet.http.Cookie;

public class UserController {
	private static Logger logger = LoggerFactory.getLogger(UserController.class);

	private static UserService uServ = new UserServiceImpl();

	public static Handler register = ctx -> {
		logger.info("A new User is making a registration request...");

		String body = ctx.body(); // .body() gets info from http request

		ObjectMapper om = new ObjectMapper(); // converts body into User object

		om.registerModule(new JavaTimeModule()); // used for time / date data types

		User target = om.readValue(body, User.class);

		logger.info("New " + target);

		boolean created = uServ.registerUser(target);

		if (created) {
			ctx.html("New User was created successfully.");
			ctx.status(HttpStatus.CREATED);
		} else {
			ctx.html("ERROR: User was not created. Please try again.");
			ctx.status(HttpStatus.NO_CONTENT);
		}
	};

	public static Handler getUserById = ctx -> {
		logger.info("A database search request has been recieved...");

		int id = Integer.parseInt(ctx.pathParam("id"));

		User target = uServ.getUserById(id);

		if (target != null && target.getUsername() != null) {
			ctx.html("User successfully retrieved from database.");
			ctx.json(target);
		} else {
			ctx.html("ERROR: Could not find User ID " + id + " in the database. Please try again.");
			ctx.status(HttpStatus.NOT_FOUND);
		}
	};

	public static Handler update = ctx -> {
		int id = Integer.parseInt(ctx.pathParam("id"));

		String body = ctx.body();

		ObjectMapper om = new ObjectMapper();

		om.registerModule(new JavaTimeModule());

		User target = om.readValue(body, User.class);

		target.setId(id);

		boolean isUpdated = uServ.updateUser(target);

		if (isUpdated == true) {
			ctx.html("User ID " + id + " information has been updated successfully.");
			ctx.status(HttpStatus.OK);
		} else {
			ctx.html("ERROR: Could not update User ID " + id + " in the database. Please try again.");
			ctx.status(HttpStatus.BAD_REQUEST);
		}
	};

	public static Handler delete = ctx -> {

		int id = Integer.parseInt(ctx.pathParam("id"));

		boolean isDeleted = uServ.deleteUser(id);

		if (isDeleted == true) {
			ctx.html("User ID " + id + " has been removed from the database successfully.");
			ctx.status(HttpStatus.OK);
		} else {
			ctx.html("ERROR: Could not delete User ID " + id + " from the database. Please try again.");
			ctx.status(HttpStatus.BAD_REQUEST);
		}

	};

	public static Handler login = ctx -> {
		String body = ctx.body();

		ObjectMapper om = new ObjectMapper();
		LoginTemplate target = om.readValue(body, LoginTemplate.class);

		boolean isAuthenicated = uServ.login(target.getUsername(), target.getPassword());

		if (isAuthenicated == true) {
			ctx.html("Successful login. Welcome " + target.getUsername() + "!");

			// jakarta.servlet.http.Cookie
			ctx.cookieStore().set("Auth-Cookie", target.getUsername() + "-56797-woof");
			Cookie auth = new Cookie("Auth-Cookie", target.getUsername() + "woof9000bark");
			ctx.res().addCookie(auth);

			ctx.status(HttpStatus.OK);
		} else {
			ctx.html("Invalid username and/or password. Please try again.");
			ctx.status(HttpStatus.UNAUTHORIZED);
		}
	};

}