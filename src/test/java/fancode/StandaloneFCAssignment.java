package fancode;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StandaloneFCAssignment
{
    // TODOS -> https://jsonplaceholder.typicode.com/todos
    // USERS -> https://jsonplaceholder.typicode.com/users

    private ExtentReports er;
    private ExtentTest et;

    private int fc_user_count = 0;
    private int fcUsersWithMoreThan50PercentTodos = 0;
    private int fcUsersWithLessThan50PercentTodos = 0;

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "http://jsonplaceholder.typicode.com";

        // Initialize ExtentReports
        ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter("reports/extentReport.html");
        htmlReporter.config().setTheme(Theme.STANDARD);
        htmlReporter.config().setDocumentTitle("FanCode SDET Assignment Report");
        htmlReporter.config().setReportName("FanCode SDET Assignment Report");

        // Additional details to be displayed in the report
        er = new ExtentReports();
        er.attachReporter(htmlReporter);
        er.setSystemInfo("Environment", "Test");
        er.setSystemInfo("Tester", "Trishul B Mody");
        er.setSystemInfo("Browser", "Chrome");
        er.setSystemInfo("Browser Version", "87.0.4280.88");
        er.setSystemInfo("Operating System", "Windows 10");
        er.setSystemInfo("Screen Resolution", "1920 x 1080");

        et = er.createTest("Verify User Completion Percentage");
    }

    @Test
    public void FC_SDET_Assignment()
    {
        List<Integer> fancodeUserIds = getFancodeUserIds();
        fc_user_count = fancodeUserIds.size();
        et.log(Status.INFO, "Total FanCode Users: " + fc_user_count);

        System.out.println("totalFancodeUsers = " + fc_user_count);

        Assert.assertFalse(fancodeUserIds.isEmpty(), "No users found in FanCode city");

        for (Integer userId : fancodeUserIds) {
            boolean isMoreThan50Percent = verifyTodoCompletionPercentage(userId);
            if (isMoreThan50Percent) {
                fcUsersWithMoreThan50PercentTodos++;
            } else {
                fcUsersWithLessThan50PercentTodos++;
            }
        }

        printReport();
    }

    private List<Integer> getFancodeUserIds() {
        Response response = RestAssured.get("/users");
        Assert.assertEquals(response.getStatusCode(), 200);

        List<Map<String, Object>> users = response.jsonPath().getList("");
        return users.stream()
                .filter(user -> {
                    Map<String, String> geo = ((Map<String, Map<String, String>>) user.get("address")).get("geo");
                    double lat = Double.parseDouble(geo.get("lat"));
                    double lng = Double.parseDouble(geo.get("lng"));
                    return lat > -40 && lat < 5 && lng > 5 && lng < 100;
                })
                .map(user -> (Integer) user.get("id"))
                .collect(Collectors.toList());
    }

    private boolean verifyTodoCompletionPercentage(Integer userId) {
        Response response = RestAssured.get("/todos?userId=" + userId);
        Assert.assertEquals(response.getStatusCode(), 200);

        List<Map<String, Object>> todos = response.jsonPath().getList("");
        long completedTasks = todos.stream()
                .filter(todo -> (Boolean) todo.get("completed"))
                .count();
        double completionPercentage = ((double) completedTasks / todos.size()) * 100;

        boolean isMoreThan50Percent = completionPercentage > 50;
        et.log(Status.INFO, "User ID " + userId + " has " + completionPercentage + "% tasks completed");

        System.out.println("User ID " + userId + " has " + completionPercentage + "% tasks completed");

        return isMoreThan50Percent;
    }

    private void printReport() {
        // Print report
        et.log(Status.INFO, "Total FanCode Users with > 50% completed tasks: " + fcUsersWithMoreThan50PercentTodos);

        System.out.println("Total FanCode Users with > 50% completed tasks: " + fcUsersWithMoreThan50PercentTodos);

        et.log(Status.INFO, "Total FanCode Users with <= 50% completed tasks: " + fcUsersWithLessThan50PercentTodos);

        System.out.println("Total FanCode Users with <= 50% completed tasks: " + fcUsersWithLessThan50PercentTodos);
    }

    @AfterClass
    public void tearDown() {
        // Close the report
        er.flush();
    }
}
