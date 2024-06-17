package steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FanCodeSteps {
    // TODOS -> https://jsonplaceholder.typicode.com/todos
    // USERS -> https://jsonplaceholder.typicode.com/users
    private List<Integer> fancodeUserIds;
    private int totalFancodeUsers;
    private int usersWithMoreThan50PercentTasks;
    private int usersWithLessThan50PercentTasks;

    @Given("User has the todo tasks")
    public void fetchAllUsers() {
        RestAssured.baseURI = "http://jsonplaceholder.typicode.com";
        Response response = RestAssured.get("/users");
        Assert.assertEquals(response.getStatusCode(), 200);

        List<Map<String, Object>> users = response.jsonPath().getList("");
        fancodeUserIds = users.stream()
                .filter(user -> {
                    Map<String, String> geo = ((Map<String, Map<String, String>>) user.get("address")).get("geo");
                    double lat = Double.parseDouble(geo.get("lat"));
                    double lng = Double.parseDouble(geo.get("lng"));
                    return lat > -40 && lat < 5 && lng > 5 && lng < 100;
                })
                .map(user -> (Integer) user.get("id"))
                .collect(Collectors.toList());

        totalFancodeUsers = fancodeUserIds.size();
        Assert.assertFalse(fancodeUserIds.isEmpty(), "No users found in FanCode city");
    }

    @When("User belongs to the city FanCode")
    public void identifyUsersFromFanCodeCity() {
        // This step is acknowledged as users are filtered in the previous step.
    }

    @Then("User Completed task percentage should be greater than 50%")
    public void verifyTaskCompletionPercentage() {
        for (Integer userId : fancodeUserIds) {
            boolean isMoreThan50Percent = verifyTodoCompletionPercentage(userId);
            if (isMoreThan50Percent) {
                usersWithMoreThan50PercentTasks++;
            } else {
                usersWithLessThan50PercentTasks++;
            }
        }

        printReport();
    }

    private boolean verifyTodoCompletionPercentage(Integer userId) {
        Response response = RestAssured.get("/todos?userId=" + userId);
        Assert.assertEquals(response.getStatusCode(), 200);

        List<Map<String, Object>> todos = response.jsonPath().getList("");
        long completedTasks = todos.stream()
                .filter(todo -> (Boolean) todo.get("completed"))
                .count();
        double completionPercentage = ((double) completedTasks / todos.size()) * 100;

        return completionPercentage > 50;
    }

    private void printReport() {
        System.out.println("Total FanCode Users: " + totalFancodeUsers);
        System.out.println("FanCode Users with > 50% completed tasks: " + usersWithMoreThan50PercentTasks);
        System.out.println("FanCode Users with <= 50% completed tasks: " + usersWithLessThan50PercentTasks);
    }
}
