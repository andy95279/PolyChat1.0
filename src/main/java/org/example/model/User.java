package org.example.model;

public class User {
    private final String id;
    private String name;
    private String surnames;
    private String email;
    private String phone;
    private String language;
    private int age;

    public User(String id, String name, String surnames, String email, String phone, String language, int age) {
        this.id = id;
        this.name = name;
        this.surnames = surnames;
        this.email = email;
        this.phone = phone;
        this.language = language;
        this.age = age;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurnames() {
        return surnames;
    }

    public void setSurnames(String surnames) {
        this.surnames = surnames;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
