package com.librarymanagement.dto;

import jakarta.validation.constraints.NotBlank;

public class MemberRequest {

    @NotBlank(message = "Member ID is required")
    private String id;

    @NotBlank(message = "Name is required")
    private String name;

    private String email;
    private String phone;

    public MemberRequest() {}

    public String getId()            { return id; }
    public void setId(String id)     { this.id = id; }
    public String getName()          { return name; }
    public void setName(String n)    { this.name = n; }
    public String getEmail()         { return email; }
    public void setEmail(String e)   { this.email = e; }
    public String getPhone()         { return phone; }
    public void setPhone(String p)   { this.phone = p; }
}
