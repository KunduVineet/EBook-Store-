package com.ebook.ebookstore.DTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserLoginDTO {

    public String email;
    public String password;
}