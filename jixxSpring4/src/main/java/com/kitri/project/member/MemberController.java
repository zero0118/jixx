package com.kitri.project.member;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Random;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.kitri.project.repository.MailHandler;

import vo.Member;

@Controller
public class MemberController {
	@Resource(name = "memService")
	private Service service;
	@Inject // spring에서 메일보내주는기능
	private JavaMailSender mailSender;

	// 문자인지 숫자인지 확인하는기능
	public static boolean isNumber(String str) {
		boolean result = false;

		try {
			Double.parseDouble(str);
			result = true;
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

	public void setService(Service service) {
		this.service = service;
	}

	// 이메일 인증 페이지 이동
	@RequestMapping(value = "verifyForm.do")
	public String verifyForm() {
		return "member/verify";
	}

	// index page이동
	@RequestMapping(value = "index.do")
	public String index() {
		return "template/index";
	}

	// loginForm으로 이동
	@RequestMapping(value = "member/loginForm.do")
	public String loginForm() {
		return "member/login";
	}

	// 회원가입 form으로 이동
	@RequestMapping(value = "member/signup.do")
	public String signupForm() {
		return "member/signup";
	}

	// 회원가입
	@RequestMapping(value = "/member/insert.do")
	public String add(@ModelAttribute("xxx") Member m, HttpServletResponse res) throws Exception {
		service.addMember(m);
		res.setContentType("text/html; charset=UTF-8");
		PrintWriter out = res.getWriter();
		out.println("<script>alert('회원가입을 축하합니다'); </script>");
		out.flush();// alert구현
		return "member/login";
	}

	// id중복검사
	@RequestMapping(value = "/idCheck.do")
	public ModelAndView idCheck(@RequestParam(value = "email") String email) {
		ModelAndView mav = new ModelAndView("member/idCheck");
		String str = "";
		Member m = service.getMemberEmail(email);
		if (m == null) {
			str = "사용가능한아이디";
		} else {
			str = "사용불가능한 아이디";
		}
		mav.addObject("str", str);
		return mav;
	}

	// 로그인기능
	@RequestMapping(value = "/login.do")
	public String login(HttpServletRequest req, Member m, HttpServletResponse res) throws Exception {
		Member m2 = service.getMemberEmail(m.getEmail());
		if (m2 == null || !m2.getPwd().equals(m.getPwd())) {
			System.out.println("로그인 실패");
			res.setContentType("text/html; charset=UTF-8");
			PrintWriter out = res.getWriter();
			out.println("<script>alert('로그인 실패'); </script>");
			out.flush();
			return "member/login";
		} else {
			HttpSession session = req.getSession();
			session.setAttribute("id", m2.getId());
			session.setAttribute("email", m.getEmail());
			return "template/index";
		}
	}

	// 회원정보수정Form으로 이동
	@RequestMapping(value = "/member/editForm.do")
	public ModelAndView editForm(HttpServletRequest req) {
		ModelAndView mav = new ModelAndView("member/editForm");
		HttpSession session = req.getSession(false);
		String id = (String) session.getAttribute("id");
		Member m = service.getMemberId(Integer.parseInt(id));
		mav.addObject("m", m);
		return mav;
	}

	// 회원정보수정기능
	@RequestMapping(value = "/member/edit.do")
	public String edit(Member m) {
		service.editMember(m);
		return "member/main";
	}

	// 로그아웃기능
	@RequestMapping(value = "/member/logout.do")
	public String logout(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		session.removeAttribute("id");
		session.invalidate();
		return "member/login";
	}
	//탈퇴기능
	@RequestMapping(value = "/member/out.do")
	public String out(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		String id = (String) session.getAttribute("id");
		service.delMember(Integer.parseInt(id));
		session.removeAttribute("id");
		session.invalidate();
		return "member/login";
	}
	//create workspace누르면 인증번호 메일전송하는 페이지로이동
	@RequestMapping(value = "crw1.do")
	public ModelAndView crw1(HttpServletRequest req) {
		ModelAndView mav = new ModelAndView("workspace/createworkspace1");
		HttpSession session = req.getSession(false);
		String email = (String) session.getAttribute("email");
		Member m = service.getMemberEmail(email);
		mav.addObject("m", m);
		return mav;
	}
	//인증번호 메일로 보내는기능
	@RequestMapping(value = "emailauth.do")
	public String emailAuth(HttpServletRequest req) throws MessagingException, UnsupportedEncodingException {
		HttpSession session = req.getSession(false);
		int id = (int) session.getAttribute("id");
		String email = (String) session.getAttribute("email");
		MailHandler sendMail = new MailHandler(mailSender);
		Random ran = new Random();
		int ran2 = 0;
		while (ran2 <= 100000) {
			ran2 = ran.nextInt(1000000);
		}
		sendMail.setSubject("JIXX 이메일인증");
		sendMail.setText(
				new StringBuffer().append("<h1>이메일인증</h1>").append("<a href='localhost:8080/project/verifyForm.do")
						.append("'target='_blenk'>이메일 인증 확인</a>").append(ran2).toString());
		sendMail.setFrom("gusdn4973@gmail.com", "jixx");
		sendMail.setTo(email);
		sendMail.send();
		service.setTempkey(ran2, id);
		return "member/verify";
	}
	//메일로보낸 인증키와 입력받은값 비교하여 메일인증
	@RequestMapping(value = "verify.do")
	public String verify(HttpServletRequest req, @RequestParam(value = "verify") int tempKey, HttpServletResponse res)
			throws Exception {
		HttpSession session = req.getSession(false);
		String email = (String) session.getAttribute("email");

		Member m = new Member();
		m.setEmail(email);
		int tempKeydb = service.selectTempKey(email);
		res.setContentType("text/html; charset=UTF-8");
		PrintWriter out = res.getWriter();
		if (tempKey == tempKeydb) {
			service.verifyMember(m);
			return "workspace/createworkspace2";
		} else {
			if (isNumber(String.valueOf(tempKey))) {
				out.println("<script>alert('인증번호가 일치하지 않습니다'); </script>");
				out.flush();
			} else {
				out.println("<script>alert('숫자를 입력하세요'); </script>");
				out.flush();
			}
		}
		return "member/verify";
	}
}
