package controller;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dao.BookDAO;
import dao.RentDAO;
import dao.UserDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import model.Book;
import model.Rent;
import util.jpa.PersistenceModule;

import javax.persistence.NoResultException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class LibraryController {
    public TextField bookIdField;
    public DatePicker deadlineDate;
    public ComboBox<String> selectUserDropdown;
    public Button rentButton;
    public Text errorInRent;

    public ComboBox<String> searchByDropdown;
    public TextField searchField;
    public Button searchButton;
    public TableView<Book> resultTable;
    public TableColumn<Book, String> bookId;
    public TableColumn<Book, String> bookTitle;
    public TableColumn<Book, String> bookAuthor;
    public TableColumn<Book, String> bookAvailability;

    public ComboBox<String> selectRentDropdown;
    public Button returnButton;

    private BookDAO bookDAO;
    private UserDAO userDAO;
    private RentDAO rentDAO;

    public void initialize() {
        Injector injector = Guice.createInjector(new PersistenceModule("jpa-persistence-unit-1"));
        bookDAO = injector.getInstance(BookDAO.class);
        userDAO = injector.getInstance(UserDAO.class);
        rentDAO = injector.getInstance(RentDAO.class);

        bookId.setCellValueFactory(new PropertyValueFactory<>("id"));
        bookTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        bookAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        bookAvailability.setCellValueFactory(new PropertyValueFactory<>("available"));

        updateDropdown(searchByDropdown, new ArrayList<>(Book.searchKeywords.keySet()));
        updateDropdown(selectUserDropdown, userDAO.findUsers());
        updateDropdown(selectRentDropdown, rentDAO.findRents());
    }

    private void updateDropdown(ComboBox<String> dropdown, List<String> options) {
        try {
            ObservableList<String> observableOptions = FXCollections.observableArrayList(options);
            dropdown.setItems(observableOptions);
        } catch (Exception e) {
            dropdown.getItems().clear();
        }
    }

    private Date castLocalDateToDate(LocalDate localDate) {
        Instant instant = Instant.from(localDate.atStartOfDay(ZoneId.systemDefault()));
        return Date.from(instant);
    }

    public void createNewRent(ActionEvent actionEvent) {
        try {
            Date currentDate = new Date(System.currentTimeMillis());
            String userNameWithId = selectUserDropdown.getValue();
            if (userNameWithId == null) {
                throw new Exception("Válassz olvasót!");
            }
            String[] splitUserName = userNameWithId.split(":");
            String bookId = bookIdField.getText();
            if (bookId.isBlank()) {
                throw new Exception("Hiányzó azonosító!");
            }
            LocalDate datePickerValue = deadlineDate.getValue();
            if (datePickerValue == null) {
                throw new Exception("Válassz lejárati dátumot!");
            }
            Date deadline = castLocalDateToDate(datePickerValue);

            Book book;
            try {
                book = bookDAO.findBookById(bookId);
            } catch (NoResultException e) {
                throw new Exception("Hibás azonosító!");
            }
            if (!book.getAvailable()) {
                throw new Exception("Ez a könyv nem kölcsönözhető ki!");
            }
            Rent rent = Rent.builder()
                    .bookId(bookId)
                    .userId(splitUserName[0])
                    .startDate(currentDate)
                    .deadline(deadline)
                    .build();
            rentDAO.persist(rent);
            bookDAO.updateAvailability(bookId, false);
            updateDropdown(selectRentDropdown, rentDAO.findRents());
            selectUserDropdown.getSelectionModel().clearSelection();
            bookIdField.clear();
            deadlineDate.getEditor().clear();
        } catch (Exception e) {
            errorInRent.setText(e.getMessage());
        }
    }

    public void search(ActionEvent actionEvent) {
        try {
            String searchTerm = searchByDropdown.getSelectionModel().getSelectedItem();
            String keyword = Book.searchKeywords.get(searchTerm);
            List<Book> resultList = bookDAO.findBooks(keyword, searchField.getText());
            if (resultList.isEmpty()) {
                resultTable.getItems().clear();
            } else {
                ObservableList<Book> result = FXCollections.observableArrayList(resultList);
                resultTable.setItems(result);
            }
        } catch (Exception ignored) {
        }
    }

    public void returnBook(ActionEvent actionEvent) {
        try {
            String[] splitSelectedRent = selectRentDropdown.getSelectionModel().getSelectedItem().split(" : ");
            Rent rent = rentDAO.findRentById(Integer.valueOf(splitSelectedRent[0]));
            bookDAO.updateAvailability(rent.getBookId(), true);
            rentDAO.remove(rent);
            updateDropdown(selectRentDropdown, rentDAO.findRents());
        } catch (Exception ignored) {
        }
    }
}
