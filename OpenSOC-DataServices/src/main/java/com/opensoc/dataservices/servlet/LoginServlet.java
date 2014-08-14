package com.opensoc.dataservices.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;

public class LoginServlet extends HttpServlet 
{
	
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		doPost( req, resp );
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		System.out.println( "Doing login here..." );
		
		String username = req.getParameter("username" );
		String password = req.getParameter("password" );
		UsernamePasswordToken token = new UsernamePasswordToken(username, password);
		
		Subject currentUser = SecurityUtils.getSubject();

		try 
		{
		    currentUser.login(token);
		} 
		catch ( UnknownAccountException uae ) 
		{
			resp.sendError(405);
		} 
		catch ( IncorrectCredentialsException ice ) 
		{
			resp.sendError(405);
		} 
		catch ( LockedAccountException lae ) 
		{
			resp.sendError(405);
		} 
		catch ( ExcessiveAttemptsException eae ) 
		{
			resp.sendError(405);
		}  
		catch ( AuthenticationException ae ) 
		{
			resp.sendError(405);
		}
		
		// resp.setHeader( "authToken", "ABC123" );
		Cookie myCookie = new Cookie("authToken", "ABC123");
		resp.addCookie(myCookie);
		
		
		// resp.setStatus(HttpServletResponse.SC_OK);
		resp.sendRedirect( "/withsocket.jsp" );
	}	
}