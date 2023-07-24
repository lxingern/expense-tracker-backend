# expense-tracker-backend
This is the backend code for the Expense Tracker app. The frontend code can be found [here](https://github.com/lxingern/expense-tracker-frontend). You can visit the app [here](http://ec2-54-169-253-204.ap-southeast-1.compute.amazonaws.com/expenses).

This web service was built using Java Spring Boot and includes endpoints for:
* Registering an account
* Logging in
* Logging out
* Viewing your expenses with optional filters for dates and categories
* Adding a new expense
* Editing an expense
* Deleting an expense

Unit tests using JUnit for the expense repository and service layer are also included. The app was deployed using AWS (RDS for the database and EC2 for the frontend and backend).
