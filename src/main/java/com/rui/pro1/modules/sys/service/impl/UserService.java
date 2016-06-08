package com.rui.pro1.modules.sys.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rui.pro1.common.bean.page.Query;
import com.rui.pro1.common.bean.page.QueryResult;
import com.rui.pro1.common.utils.copyo.BeanCopierUtils;
import com.rui.pro1.modules.sys.bean.UserBean;
import com.rui.pro1.modules.sys.entity.Menu;
import com.rui.pro1.modules.sys.entity.Role;
import com.rui.pro1.modules.sys.entity.User;
import com.rui.pro1.modules.sys.exception.UserExistException;
import com.rui.pro1.modules.sys.mapper.MenuMapper;
import com.rui.pro1.modules.sys.mapper.UserMapper;
import com.rui.pro1.modules.sys.service.IUserService;
import com.rui.pro1.modules.sys.utils.PassUtil;
import com.rui.pro1.modules.sys.vo.UserLoginVo;
import com.rui.pro1.modules.sys.vo.UserVo;

@Service
public class UserService implements IUserService {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private MenuMapper menuMapper;

	@Override
	public QueryResult<UserBean> getUserList(int page, int pagesize, UserVo userVo) {
		// User user=new User();
		// user.setId(1);
		// user.setName("user001");
		// user.setPassowd("pass");
		//
		// List<User> list=new ArrayList<User>();
		// list.add(user);

		Query query = new Query();
		query.setBean(userVo);
		query.setPageIndex(page);
		query.setPageSize(pagesize);

		// 组合分页信息
		QueryResult<UserBean> queryResult = new QueryResult<UserBean>();
		Long count = userMapper.getCount(query);
		List<User> list = userMapper.queryPages(query);
		
		List<UserBean> listBean =new ArrayList<UserBean>();
		if(list!=null&&list.size()>0){
			for(int i=0;i<list.size();i++)
			{
				User user=list.get(i);
				if(user!=null&&user.getId()>0)
				{
					UserBean userBean=new UserBean();
					BeanCopierUtils.copyProperties(user, userBean);
					listBean.add(userBean);
				}
				
				
			}
		}
		// 总页数 和 取多少条
		queryResult.setPages(count, pagesize);
		queryResult.setItems(listBean);

		return queryResult;
	}

	@Override
	public UserBean get(int userId) {
		User user= userMapper.get(userId);
		UserBean userBean=new UserBean();
		BeanCopierUtils.copyProperties(user, userBean);
		return userBean;
	}

	@Override
	public int del(int userId) {
		return userMapper.del(userId);
	}

	@Override
	public int add(User user) throws UserExistException {
		// 为开发速度 不考虑 分离转换
		// User user=new User();
		// BeanCopier copier = BeanCopier.create(UserVo.class, User.class,
		// false);
		// copier.copy(userVo, user, null);

		if (user == null ||StringUtils.isBlank(user.getUserName())||StringUtils.isBlank(user.getPassword())|| user.getRoles() == null
				|| user.getRoles().size() <= 0) {
			return 0;
		}
		
		
		int count =0;
		
		if(user.getId()!=null&&user.getId()>0)
		{//修改
			
			
			if(!StringUtils.isBlank(user.getPassword()))
			{
				user.setPassword(PassUtil.encryptPassword(user.getPassword(),user.getUserName()));

			}
			
			
			 count = userMapper.updateByPrimaryKeySelective(user);
			// 用户拥有的角色
			if (count > 0) {
				
				// 删除用户拥有的角色
				userMapper.delUserRole(user.getId());
				// 如果没有角色更新的 返回
				if (user.getRoles() == null || user.getRoles().size() <= 0) {
					return count;
				}
				// 用户拥有的角色
				for (Role role : user.getRoles()) {
					if (role.getId()!=null&& role.getId()> 0) {
						userMapper.addUserRole(user.getId(), role.getId());
					}
				}

			}
			
		}else{//新增
			//登录名是否存在
			UserLoginVo vo=new UserLoginVo();
			vo.setUserName(user.getUserName());
			User isExistsUser=userMapper.query(vo);
			if(isExistsUser!=null&&isExistsUser.getId()>0){
				throw new UserExistException("用户已存在");
			}
			//
			user.setPassword(PassUtil.encryptPassword(user.getPassword(),user.getUserName()));
			
			 count = userMapper.insertSelective(user);
			if (count > 0) {
				// 用户拥有的角色
				for (Role role : user.getRoles()) {
					if (role.getId()!=null&& role.getId()> 0) {
						int countx=userMapper.addUserRole(user.getId(), role.getId());
//						if(countx<=0){
//							throw new SysRuntimeException("添加角色失败");
//						}
						
					}
				}
				// 用户to部门 
//				if (user.getDepartmentId()!=null&&user.getDepartmentId() > 0) {
//					userMapper.addUserDepartment(user.getId(),
//							user.getDepartmentId());
//				}
			}
			
		}
		
		
		
	

		
		
		
		return count;
	}

	@Override
	public int update(User user) {
		if (user == null) {
			return 0;
		}
		
		if(!StringUtils.isBlank(user.getPassword()))
		{
			user.setPassword(PassUtil.encryptPassword(user.getPassword(),user.getUserName()));

		}
		

		int count = userMapper.updateByPrimaryKeySelective(user);

		// 用户拥有的角色
		if (count > 0) {
			
			// 删除用户拥有的角色
			userMapper.delUserRole(user.getId());
						
			// 如果没有角色更新的 返回
			if (user.getRoles() == null || user.getRoles().size() <= 0) {
				return count;
			}
			
			// 用户拥有的角色
			for (Role role : user.getRoles()) {
				if (role.getId()!=null&&role.getId() > 0) {
					userMapper.addUserRole(user.getId(), role.getId());
				}
			}
			// 用户to部门
//			if (user.getDepartmentId() > 0) {
//				User user2 = userMapper.get(user.getId());
//
//				if (user2 == null || user2.getId() <= 0) {
//					// FIXME:抛异常 回滚
//				}
//				// 如果部门有改变，才修改
//				if (user2.getDepartmentId() != user.getDepartmentId()) {
//					userMapper.delUserDepartment(user.getId());
//					userMapper.addUserDepartment(user.getId(),
//							user.getDepartmentId());
//				}
//			}
		}
		return count;
	}
	
	

	@Override
	public UserBean getUser(String username) {

		UserLoginVo userLoginVo = new UserLoginVo();
		userLoginVo.setUserName(username);

		User user = userMapper.query(userLoginVo);
		UserBean userBean=new UserBean();
		
		BeanCopierUtils.copyProperties(user, userBean);
	
		

		return userBean;
	}

	@Override
	public Set<String> getUserRole(String username) {
		UserLoginVo userLoginVo = new UserLoginVo();
		userLoginVo.setUserName(username);

		User user = userMapper.query(userLoginVo);

		if (user == null) {
			return null;
		}

		if (user.getRoles() == null || user.getRoles().size() <= 0) {
			return null;
		}

		Set<String> roles = new HashSet<String>();
		for (Role role : user.getRoles()) {
			roles.add(role.getName());
		}
		return roles;
	}

	@Override
	public Set<String> getUserPermissions(String username) {

		UserLoginVo userLoginVo = new UserLoginVo();
		userLoginVo.setUserName(username);
		User user = userMapper.query(userLoginVo);
		if (user == null) {
			return null;
		}

		if (user.getRoles() == null || user.getRoles().size() <= 0) {
			return null;
		}
		Set<String> result = new HashSet<String>();
		for (Role role : user.getRoles()) {
			List<Menu> mes = menuMapper.getAllMenuByRoleId(role.getId());
			if (mes == null || mes.size() <= 0) {
				continue;
			}
			for (Menu m : mes) {
				
				//FIXME
				//result.add(m.getPermission());
				result.add(m.getId());
			}
		}

		return result;
	}

	@Override
	public List<Menu> getUserMenus(String username) {

//		UserLoginVo userLoginVo = new UserLoginVo();
//		userLoginVo.setUserName(username);
		//User user = userMapper.query(userLoginVo);
		
		User user = userMapper.queryByUserName(username);
		
		if (user == null) {
			return null;
		}

		if (user.getRoles() == null || user.getRoles().size() <= 0) {
			return null;
		}

		List<Menu> menus = new ArrayList<Menu>();
		for (Role role : user.getRoles()) {
			List<Menu> menu = menuMapper.getAllMenuByRoleId(role.getId());
			if (menu == null || menu.size() <= 0) {
				continue;
			}
			
			for(Menu m:menu){
				if(m==null||m.getStatus()!=0){
					continue;
				}
				menus.add(m);
			}
			//menus.addAll(menu);

		}

		return menus;
	}



}
