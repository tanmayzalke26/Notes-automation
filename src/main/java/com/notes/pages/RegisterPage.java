package com.notes.pages;

import com.notes.base.BasePage;
import org.openqa.selenium.By;

/**
 * RegisterPage — handles new account registration.
 */
public class RegisterPage extends BasePage {

    private final By nameField        = By.cssSelector("input[data-testid='register-name']");
    private final By emailField       = By.cssSelector("input[data-testid='register-email']");
    private final By passwordField    = By.cssSelector("input[data-testid='register-password']");
    private final By confirmPassField = By.cssSelector("input[data-testid='register-confirm-password']");
    private final By registerButton   = By.cssSelector("button[data-testid='register-submit']");
    private final By successMessage   = By.cssSelector("div[data-testid='alert-message']");

    public RegisterPage enterName(String name) {
        type(nameField, name);
        return this;
    }

    public RegisterPage enterEmail(String email) {
        type(emailField, email);
        return this;
    }

    public RegisterPage enterPassword(String password) {
        type(passwordField, password);
        return this;
    }

    public RegisterPage enterConfirmPassword(String password) {
        type(confirmPassField, password);
        return this;
    }

    public RegisterPage clickRegister() {
        click(registerButton);
        return this;
    }

    public String getSuccessMessage() {
        return getText(successMessage);
    }

    public boolean isSuccessMessageDisplayed() {
        return isPresent(successMessage);
    }
}
