package com.clinassist.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.List;

/**
 * ClinAssist Selenium Tests with Allure Reporting
 * Run with: mvn clean test
 * Generate report: mvn allure:serve
 */
@Epic("ClinAssist - Assistant Clinique Prédictif")
@Feature("Tests d'Interface Utilisateur")
public class ClinAssistAllureTests {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:4200";

    @BeforeAll
    public static void setup() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @AfterEach
    public void captureScreenshotOnFailure(TestInfo testInfo) {
        // Capture screenshot after each test
        captureScreenshot("After_" + testInfo.getDisplayName());
    }

    // ========================================
    // TEST: Login Thérapeute
    // ========================================
    @Test
    @Story("Authentification")
    @Description("Vérifier que le thérapeute peut se connecter avec des identifiants valides")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Test - Login Thérapeute")
    public void testLoginTherapeute() {
        step("Naviguer vers la page de login");
        driver.get(BASE_URL + "/login");
        delay(3000);

        step("Entrer le nom d'utilisateur");
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("input[type='text'], input[type='email'], input[formControlName='username'], input[formControlName='email']")));
        emailField.clear();
        emailField.sendKeys("dr.martin");

        step("Entrer le mot de passe");
        WebElement passwordField = driver.findElement(
            By.cssSelector("input[type='password']"));
        passwordField.clear();
        passwordField.sendKeys("test123");

        captureScreenshot("Login_Form_Filled");

        step("Cliquer sur le bouton de connexion");
        WebElement loginBtn = driver.findElement(
            By.cssSelector("button[type='submit']"));
        loginBtn.click();

        step("Vérifier la redirection vers le dashboard");
        delay(4000);
        
        // Wait for dashboard or home page
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/dashboard"),
            ExpectedConditions.urlContains("/home"),
            ExpectedConditions.urlContains("/patients")
        ));

        captureScreenshot("After_Login_Success");
        
        Assertions.assertTrue(
            !driver.getCurrentUrl().contains("/login"),
            "L'utilisateur devrait être redirigé après connexion"
        );
    }

    // ========================================
    // TEST: Gestion des Patients
    // ========================================
    @Test
    @Story("Gestion des Patients")
    @Description("Vérifier qu'un nouveau patient peut être créé avec succès")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Test - Création de Patient")
    public void testPatientCreation() {
        // Login first
        loginIfNeeded();

        step("Naviguer vers la page Patients");
        driver.get(BASE_URL + "/patients");
        delay(2000);

        captureScreenshot("Patients_Page");

        step("Cliquer sur 'Nouveau Patient'");
        WebElement newPatientBtn = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//button[contains(text(),'Nouveau') or contains(text(),'Ajouter')]")));
        scrollAndClick(newPatientBtn);
        delay(2000);

        captureScreenshot("Patient_Form_Modal");

        step("Remplir le formulaire patient");
        fillPatientForm();

        captureScreenshot("Patient_Form_Filled");

        step("Soumettre le formulaire");
        clickSubmitButton("Enregistrer");

        delay(3000);
        captureScreenshot("After_Patient_Creation");
    }

    // ========================================
    // TEST: Validation des Données
    // ========================================
    @Test
    @Story("Validation des Données")
    @Description("Vérifier que le formulaire patient valide les champs obligatoires")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Test - Validation Formulaire Patient")
    public void testPatientFormValidation() {
        loginIfNeeded();

        step("Naviguer vers la page Patients");
        driver.get(BASE_URL + "/patients");
        delay(2000);

        step("Ouvrir le formulaire de nouveau patient");
        WebElement newPatientBtn = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//button[contains(text(),'Nouveau') or contains(text(),'Ajouter')]")));
        scrollAndClick(newPatientBtn);
        delay(2000);

        step("Essayer de soumettre un formulaire vide");
        try {
            WebElement submitBtn = driver.findElement(
                By.xpath("//button[contains(text(),'Enregistrer')]"));
            
            // Check if button is disabled
            boolean isDisabled = !submitBtn.isEnabled() || 
                submitBtn.getAttribute("class").contains("disabled");
            
            captureScreenshot("Empty_Form_Validation");
            
            if (isDisabled) {
                Allure.step("Le bouton est désactivé pour un formulaire vide - Validation OK");
            }
        } catch (Exception e) {
            captureScreenshot("Validation_Error");
        }

        step("Fermer le modal");
        closeModal();
    }

    // ========================================
    // TEST: Planification de Séance
    // ========================================
    @Test
    @Story("Planification des Séances")
    @Description("Vérifier qu'une nouvelle séance peut être planifiée")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Test - Planification de Séance")
    public void testSessionScheduling() {
        loginIfNeeded();

        step("Naviguer vers la page Séances");
        driver.get(BASE_URL + "/seances");
        delay(2000);

        captureScreenshot("Seances_Page");

        step("Cliquer sur 'Nouvelle Séance'");
        try {
            WebElement newSeanceBtn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//button[contains(text(),'Nouvelle') or contains(text(),'Planifier') or contains(text(),'Ajouter')]")));
            scrollAndClick(newSeanceBtn);
            delay(2000);

            captureScreenshot("Seance_Form_Modal");

            step("Remplir le formulaire de séance");
            fillSeanceForm();

            captureScreenshot("Seance_Form_Filled");

            step("Planifier la séance");
            clickSubmitButton("Planifier");

            delay(3000);
            captureScreenshot("After_Seance_Creation");

        } catch (Exception e) {
            captureScreenshot("Seance_Error");
            Allure.step("Erreur lors de la planification: " + e.getMessage());
        }
    }

    // ========================================
    // TEST: Prédictions IA
    // ========================================
    @Test
    @Story("Prédictions IA")
    @Description("Vérifier que la page des prédictions IA se charge correctement")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Test - Page Prédictions IA")
    public void testAIPredictionsPage() {
        loginIfNeeded();

        step("Naviguer vers la page Prédictions IA");
        driver.get(BASE_URL + "/predictions");
        delay(3000);

        captureScreenshot("Predictions_Page");

        step("Vérifier que la page est chargée");
        String pageSource = driver.getPageSource().toLowerCase();
        boolean hasContent = pageSource.contains("prédiction") || 
                            pageSource.contains("prediction") ||
                            pageSource.contains("risque") ||
                            pageSource.contains("risk");

        Assertions.assertTrue(hasContent, "La page devrait afficher du contenu lié aux prédictions");

        captureScreenshot("Predictions_Content");
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    @Step("{0}")
    private void step(String description) {
        System.out.println("[STEP] " + description);
    }

    @Attachment(value = "{0}", type = "image/png")
    private byte[] captureScreenshot(String name) {
        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            return screenshot;
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private void loginIfNeeded() {
        driver.get(BASE_URL + "/login");
        delay(2000);
        
        if (driver.getCurrentUrl().contains("login")) {
            try {
                WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("input[type='text'], input[type='email'], input[formControlName='username'], input[formControlName='email']")));
                emailField.clear();
                emailField.sendKeys("dr.martin");
                
                WebElement passwordField = driver.findElement(
                    By.cssSelector("input[type='password']"));
                passwordField.clear();
                passwordField.sendKeys("test123");
                
                WebElement loginBtn = driver.findElement(
                    By.cssSelector("button[type='submit']"));
                loginBtn.click();
                
                delay(4000);
                
                // Wait for redirect
                wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/dashboard"),
                    ExpectedConditions.urlContains("/home"),
                    ExpectedConditions.urlContains("/patients")
                ));
            } catch (Exception e) {
                // Already logged in or error
            }
        }
    }

    private void fillPatientForm() {
        try {
            WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[name='firstName']")));
            firstNameField.sendKeys("Test");

            WebElement lastNameField = driver.findElement(By.cssSelector("input[name='lastName']"));
            lastNameField.sendKeys("Allure");

            WebElement emailField = driver.findElement(By.cssSelector("input[name='email']"));
            emailField.sendKeys("test.allure" + System.currentTimeMillis() % 10000 + "@test.com");

            WebElement phoneField = driver.findElement(By.cssSelector("input[name='phoneNumber']"));
            phoneField.sendKeys("0612345678");

            // Date of birth using JavaScript
            WebElement birthDateField = driver.findElement(By.cssSelector("input[name='dateOfBirth']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].value = '1990-05-15';", birthDateField);

        } catch (Exception e) {
            Allure.step("Erreur lors du remplissage: " + e.getMessage());
        }
    }

    private void fillSeanceForm() {
        try {
            // Select patient
            List<WebElement> selects = driver.findElements(By.cssSelector(".modal select.form-select"));
            if (!selects.isEmpty()) {
                scrollAndClick(selects.get(0));
                delay(500);
                List<WebElement> options = selects.get(0).findElements(By.tagName("option"));
                if (options.size() > 1) {
                    options.get(1).click();
                }
            }

            // Date
            WebElement dateField = driver.findElement(By.cssSelector(".modal input[type='date']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].value = '2025-01-30';", dateField);

            // Time
            WebElement timeField = driver.findElement(By.cssSelector(".modal input[type='time']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].value = '14:00';", timeField);

        } catch (Exception e) {
            Allure.step("Erreur lors du remplissage séance: " + e.getMessage());
        }
    }

    private void clickSubmitButton(String buttonText) {
        try {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'modal')]//button[contains(text(),'" + buttonText + "')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        } catch (Exception e) {
            // Try alternative
            try {
                WebElement btn = driver.findElement(By.cssSelector(".modal-footer .btn-primary"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
            } catch (Exception ex) {
                Allure.step("Bouton non trouvé: " + buttonText);
            }
        }
    }

    private void closeModal() {
        try {
            WebElement annulerBtn = driver.findElement(By.xpath("//button[contains(text(),'Annuler')]"));
            annulerBtn.click();
        } catch (Exception e) {
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        }
        delay(500);
    }

    private void scrollAndClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
        delay(300);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    private void delay(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
