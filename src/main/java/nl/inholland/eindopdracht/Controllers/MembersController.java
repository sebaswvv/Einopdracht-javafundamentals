package nl.inholland.eindopdracht.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import nl.inholland.eindopdracht.Controllers.Events.MouseHoverEvent;
import nl.inholland.eindopdracht.Data.Database;
import nl.inholland.eindopdracht.Models.Member;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MembersController extends MouseHoverEvent {
    @FXML
    public TableView<Member> memberTable;
    @FXML
    public TableColumn<Member, String> firstNameColumn;
    @FXML
    public TableColumn<Member, String> lastNameColumn;
    @FXML
    public TableColumn<Member, String> birthDateColumn;
    @FXML
    public Label errorLabel;
    @FXML
    public TextField memberIDDeleteField;
    @FXML
    public TextField firstNameField;
    @FXML
    public TextField searchField;
    @FXML
    public TextField lastNameField;
    @FXML
    public DatePicker birthDatePicker;

    private final Database DATABASE;

    public MembersController(Database database) {
        this.DATABASE = database;
    }

    @FXML
    public void initialize() {
        setTableItems(this.DATABASE.getMEMBERS());
        // add event listener for search function
        searchField.textProperty().addListener((observable, oldValue, newValue) -> searchTextFieldChanges(newValue));

        memberTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        memberTable.setPlaceholder(new Label("Er zijn geen medewerkers gevonden"));

        // make table editable
        memberTable.setEditable(true);
        firstNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        lastNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        birthDateColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        setOnEditEventHandlers();
    }

    private void searchTextFieldChanges(String newValue) {
        ArrayList<Member> allMembers = (ArrayList<Member>) this.DATABASE.getMEMBERS();
        ArrayList<Member> matchingMembers = new ArrayList<>();

        // find the items that start with the search query
        for (Member member : allMembers) {
            if (member.getFirstName().toLowerCase().startsWith(newValue.toLowerCase()) || member.getLastName().toLowerCase().startsWith(newValue.toLowerCase())) {
                matchingMembers.add(member);
            }
        }
        setTableItems(matchingMembers);
    }

    private void setOnEditEventHandlers() {
        birthDateColumn.setOnEditCommit(this::editBirthDate);

        firstNameColumn.setOnEditCommit(event -> {
            Member member = event.getRowValue();
            member.setFirstName(event.getNewValue());
            this.DATABASE.editMember(member);
        });

        lastNameColumn.setOnEditCommit(event -> {
            Member member = event.getRowValue();
            member.setLastName(event.getNewValue());
            this.DATABASE.editMember(member);
        });
    }

    private void editBirthDate(TableColumn.CellEditEvent<Member, String> event) {
        // check if the birthdate is in the correct format (dd-MM-yyyy)
        if (event.getNewValue().matches("\\d{2}-\\d{2}-\\d{4}")) {
            Member member = event.getRowValue();

            // parse the string to a date
            String[] dateParts = event.getNewValue().split("-");
            Calendar dateOfBirth = Calendar.getInstance();
            dateOfBirth.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateParts[0]));
            dateOfBirth.set(Calendar.MONTH, Integer.parseInt(dateParts[1]) - 1);
            dateOfBirth.set(Calendar.YEAR, Integer.parseInt(dateParts[2]));

            // check if the date is in the past
            if (dateOfBirth.before(Calendar.getInstance())) {
                member.setDateOfBirth(dateOfBirth);
                DATABASE.editMember(member);
            } else {
                errorLabel.setVisible(true);
                errorLabel.setText("Birthdate can't be in the future");

                // reset the birthdate to the old value
                resetBirthDateColum();
            }
        } else {
            errorLabel.setVisible(true);
            errorLabel.setText("Birthdate must be in the format dd-MM-yyyy");

            // reset the birthdate to the old value
            resetBirthDateColum();
        }
    }

    private void setTableItems(List<Member> members) {
        members = FXCollections.observableArrayList(members);
        memberTable.setItems((ObservableList<Member>) members);
    }

    @FXML
    public void addMemberButton() {
        errorLabel.setVisible(false);

        // check if all fields are filled in
        if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() || birthDatePicker.getValue() == null) {
            errorLabel.setVisible(true);
            errorLabel.setText("Please fill in all fields");
            return;
        }

        // check if the birthdate is in the past
        if (birthDatePicker.getValue().isAfter(Calendar.getInstance().getTime().toInstant().atZone(Calendar.getInstance().getTimeZone().toZoneId()).toLocalDate())) {
            errorLabel.setVisible(true);
            errorLabel.setText("Birthdate can't be in the future");
            resetBirthDateColum();
            birthDatePicker.setValue(null);
            return;
        }

        createAndSaveNewMember();
        setTableItems(this.DATABASE.getMEMBERS());
    }

    private void resetBirthDateColum() {
        // reset the birthdate to the old value
        birthDateColumn.setVisible(false);
        birthDateColumn.setVisible(true);
    }

    private void createAndSaveNewMember() {
        // get all the information from the textFields and create the new member
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        Calendar birthDate = Calendar.getInstance();
        birthDate.set(birthDatePicker.getValue().getYear(), birthDatePicker.getValue().getMonthValue(), birthDatePicker.getValue().getDayOfMonth());

        firstNameField.clear();
        lastNameField.clear();
        birthDatePicker.getEditor().clear();

        this.DATABASE.addMember(firstName, lastName, birthDate);
    }

    // Word gebruikt door FXML file!
    @FXML
    public void deleteMemberButton() {
        errorLabel.setVisible(false);

        // check if the memberID is filled in / is a number / exists in the database
        if (memberIDDeleteField.getText().isEmpty()) {
            errorLabel.setVisible(true);
            errorLabel.setText("Please fill in the member ID");
            return;
        } else if (!memberIDDeleteField.getText().matches("\\d")) {
            errorLabel.setVisible(true);
            errorLabel.setText("Member ID must be a number");
            return;
        } else if (!DATABASE.memberExists(Integer.parseInt(memberIDDeleteField.getText()))) {
            errorLabel.setVisible(true);
            errorLabel.setText("Member does not exist");
            return;
        }
        int memberID = Integer.parseInt(memberIDDeleteField.getText());

        // show prompt to confirm deletion
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete item");
        alert.setHeaderText("Are you sure you want to delete item " + memberID + " ?");
        alert.setContentText("This action cannot be undone");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                this.DATABASE.deleteMember(memberID);
                setTableItems(this.DATABASE.getMEMBERS());
            }
        });
        memberIDDeleteField.clear();
    }
}
