package com.rui.pro1.common.annotatiions;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.shiro.authz.annotation.Logical;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
//@Inherited
public @interface PermissionAnnot 
{
	public String id() default "-1";
	//public String menuId() default "-1";
	public String name() default "";
	public String cls() default "";
	public int order() default -1;
	
	 /**
     * The permission string which will be passed to {@link org.apache.shiro.subject.Subject#isPermitted(String)}
     * to determine if the user is allowed to invoke the code protected by this annotation.
     */
   // String[] value();
    
    /**
     * The logical operation for the permission checks in case multiple roles are specified. AND is the default
     * @since 1.1.0
     */
    //Logical logical() default Logical.AND; 

}
