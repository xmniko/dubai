package com.emix.dubai.web.controller.system;

import com.emix.dubai.web.controller.BaseController;
import com.emix.dubai.constants.GlobalConstants;
import com.emix.dubai.business.entity.system.User;
import com.emix.core.exception.ServiceException;
import com.emix.dubai.business.service.system.UserService;
import com.emix.dubai.web.form.passport.UserForm;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springside.modules.web.Servlets;

import javax.servlet.ServletRequest;
import javax.validation.Valid;
import java.util.Map;

/**
 * 管理员管理用户的Controller.
 *
 * @author calvin
 */
@Controller
@RequestMapping(value = "/system/user")
public class UserAdminController extends BaseController {

    private static Map<String, String> sortTypes = Maps.newLinkedHashMap();

    static {
        sortTypes.put("auto", "自动");
        sortTypes.put("loginName", "登录名");
        sortTypes.put("name", "用户名");
    }

    @Autowired
    private UserService userService;

    @RequestMapping(value = "")
    public String list(@RequestParam(value = "sortType", defaultValue = "auto") String sortType,
                       @RequestParam(value = "page", defaultValue = "1") int pageNumber, Model model, ServletRequest request) {
        Map<String, Object> searchParams = Servlets.getParametersStartingWith(request, "search_");

        Page<User> users = userService.getUsers(searchParams, pageNumber, GlobalConstants.PAGE_SIZE, sortType);

        model.addAttribute("users", users);
        model.addAttribute("sortType", sortType);
        model.addAttribute("sortTypes", sortTypes);
        // 将搜索条件编码成字符串，用于排序，分页的URL
        model.addAttribute("searchParams", Servlets.encodeParameterStringWithPrefix(searchParams, "search_"));

        return "system/userList";
    }

    @RequestMapping(value = "create", method = RequestMethod.GET)
    public String createForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("action", "create");
        return "system/userForm";
    }

    @RequestMapping(value = "create", method = RequestMethod.POST)
    public String create(@Valid UserForm userForm, RedirectAttributes redirectAttributes) {
        User user = userForm.getUser();
        String plainPassword = userForm.getPlainPassword();

        userService.createUser(user, plainPassword);

        redirectAttributes.addFlashAttribute("username", user.getLoginName());
        return redirect("/system/user");
    }

    @RequestMapping(value = "update/{id}", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("user", userService.getUser(id));
        model.addAttribute("action", "update");
        return "system/userForm";
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    public String update(@Valid @ModelAttribute("preloadUser") UserForm userForm, RedirectAttributes redirectAttributes) {
        User user = userForm.getUser();
        String plainPassword = userForm.getPlainPassword();

        userService.updateUser(user, plainPassword);

        redirectAttributes.addFlashAttribute("message", "更新用户" + user.getLoginName() + "成功");
        return redirect("/system/user");
    }

    @RequestMapping(value = "delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        User user = userService.getUser(id);
        try {
            userService.deleteUser(id);
        } catch (ServiceException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return redirect("/system/user");
        }
        redirectAttributes.addFlashAttribute("message", "删除用户" + user.getLoginName() + "成功");
        return redirect("/system/user");
    }

    @RequestMapping(value = "active/{id}")
    public String active(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        User user = userService.getUser(id);
        userService.activeUser(id);
        redirectAttributes.addFlashAttribute("message", "激活用户" + user.getLoginName() + "成功");
        return redirect("/system/user");
    }

    @RequestMapping(value = "deactive/{id}")
    public String deactive(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        User user = userService.getUser(id);
        try {
            userService.deactiveUser(id);
        } catch (ServiceException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return redirect("/system/user");
        }
        redirectAttributes.addFlashAttribute("message", "取消激活用户" + user.getLoginName() + "成功");
        return redirect("/system/user");
    }

    /**
     * 使用@ModelAttribute, 实现Struts2 Preparable二次部分绑定的效果,先根据form的id从数据库查出User对象,再把Form提交的内容绑定到该对象上。
     * 因为仅update()方法的form中有id属性，因此本方法在该方法中执行.
     */
    @ModelAttribute("preloadUser")
    public User getUser(@RequestParam(value = "id", required = false) Long id) {
        if (id != null) {
            return userService.getUser(id);
        }
        return null;
    }

    /**
     * Ajax请求校验loginName是否唯一。
     */
    @RequestMapping(value = "checkLoginName")
    @ResponseBody
    public String checkLoginName(@RequestParam("loginName") String loginName) {
        if (userService.findUserByLoginName(loginName) == null) {
            return "true";
        } else {
            return "false";
        }
    }
}
