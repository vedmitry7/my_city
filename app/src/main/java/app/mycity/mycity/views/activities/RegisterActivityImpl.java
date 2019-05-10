package app.mycity.mycity.views.activities;

public interface RegisterActivityImpl {

    void setPassword(String password, String confirm);
    void setEmail(String email);
    void setInfo(String firstName, String secondName, String birthday, String sex, String cityId);
    void setCode(String code);
    void nextConfirmEmailCodeStep();
    void checkEmail();
    void checkEmailCodeAndRegistration();
    void commitPassword();
}
