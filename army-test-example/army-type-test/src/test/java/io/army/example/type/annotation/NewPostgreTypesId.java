package io.army.example.type.annotation;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Qualifier("newPostgreTypeId")
public @interface NewPostgreTypesId {

}
