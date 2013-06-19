package com.emix.dubai.business.entity;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.emix.dubai.business.entity.sys.User;
import org.hibernate.validator.constraints.NotBlank;

//JPA标识
@javax.persistence.Entity
@Table(name = "dubai_task")
public class Task extends BaseEntity {

	private String title;
	private String description;
	private User user;

	// JSR303 BeanValidator的校验规则
	@NotBlank
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	// JPA 基于USER_ID列的多对一关系定义
	@ManyToOne
	@JoinColumn(name = "user_id")
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}