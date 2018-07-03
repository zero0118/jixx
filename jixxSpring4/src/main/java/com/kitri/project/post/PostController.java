package com.kitri.project.post;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import net.coobird.thumbnailator.Thumbnails;
import vo.Channel;
import vo.Post;

@Controller
public class PostController implements ApplicationContextAware {
	@Resource(name = "postService")
	private Service service;
	private WebApplicationContext context = null;

	@RequestMapping(value = "/post/write.do")
	public String write(Post post, HttpServletRequest req, @RequestParam(value = "ch_list") String ch_list,
			@RequestParam(value = "nicknamelist") String nicknamelist, RedirectAttributes redirectAttributes) {
		HttpSession session = req.getSession(false);
		redirectAttributes.addAttribute("nicknamelist", nicknamelist);
		redirectAttributes.addAttribute("ch_list", ch_list);
		MultipartFile file = post.getFile();
		if (file.getOriginalFilename() != "") {
			int pos = file.getOriginalFilename().lastIndexOf(".");
			String ext = file.getOriginalFilename().substring(pos);
			String name = file.getOriginalFilename().substring(0, pos);
			String path = "D:\\files\\" + name + UUID.randomUUID().toString() + ext;
			String thumbnailPath = "D:\\git\\jixxSpring4\\src\\main\\webapp\\resources\\img\\" + name + UUID.randomUUID().toString();
			File f = new File(path);
			try {
				file.transferTo(f);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			post.setFile_original(path);
			if (ext.equals(".jpg")) {
				File thumbnail = new File(thumbnailPath);
				if (f.exists()) {
					thumbnail.getParentFile().mkdirs();
					try {
						Thumbnails.of(f).size(190, 150).outputFormat("png").toFile(thumbnail);
						post.setFile_thumbnail(thumbnail.getName() + ".png");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}				
			}
		} else {
			post.setFile_original("x");
		}
		post.setUser_id(Integer.parseInt(session.getAttribute("id").toString()));
		service.write(post);
		return "redirect:/post/list.do?page=1&cn=1";
	}

	@RequestMapping(value = "/post/list.do", method = RequestMethod.GET)
	public ModelAndView list(HttpServletRequest req, @RequestParam(value = "page") int page,
			@RequestParam(value = "cn") int cn, @RequestParam(value = "ch_list") String ch_list,
			@RequestParam(value = "nicknamelist") String nicknamelist) {
		HttpSession session = req.getSession(false);
		int id = (int) session.getAttribute("id");
		int rep_id = (int) session.getAttribute("rep_id");
		ArrayList<Post> list = service.show(page, cn);
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getPost_status() == 0) {
				list.get(i).setFile_original("삭제된 파일입니다.");
				list.get(i).setContent("삭제된 글입니다.");
				list.get(i).setNickname("삭제된 글입니다.");
			} else if (list.get(i).getFile_original() != null && !list.get(i).getFile_original().equals("x")) {
				String str = list.get(i).getFile_original();
				String name = str.substring(0, str.length() - 40);
				String realName = name.substring(9);
				String uuidName = str.substring(9);
				int pos = str.lastIndexOf(".");
				String ext = str.substring(pos);
				list.get(i).setFileName(realName + ext);
				list.get(i).setFile_original(uuidName);
			}
		}
		ModelAndView mav = new ModelAndView("/template/main");
		Channel ch = service.getChannel(cn);
		
		mav.addObject("ch", ch);
		mav.addObject("id", id);
		mav.addObject("rep_id", rep_id);
		mav.addObject("ch_list", ch_list);
		mav.addObject("nicknamelist", nicknamelist);
		mav.addObject("list", list);
		return mav;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = (WebApplicationContext) applicationContext;
	}

	@RequestMapping(value = "/post/download.do", method = RequestMethod.GET)
	public ModelAndView fileDown(@RequestParam("fileName") String fileName) {
		String fullPath = "D:\\files\\" + fileName;
		File downloadFile = new File(fullPath);
		return new ModelAndView("download", "downloadFile", downloadFile);
	}

	@RequestMapping(value = "/post/delete.do")
	public String delete(@RequestParam("post_id") int post_id) {
		service.delete(post_id);
		return "template/main";
	}

}