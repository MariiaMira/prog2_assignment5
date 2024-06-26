import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.Set;
import java.util.function.Predicate;

public class Exercise5 extends Application {

	private final Database db = new Database();
	private final ObservableList<Recording> recordings = FXCollections.observableArrayList(db.getRecordings());
	private final FilteredList<Recording> filteredList = new FilteredList<>(recordings, predicate -> true);

	private final GridPane filters = new GridPane();
	private final TitledPane filterPane = new TitledPane("Filter", filters);

	private final TableView<Recording> table = new TableView<>(filteredList);

	private final Spinner<Integer> minYearSpinner = new Spinner<>();
	private final Spinner<Integer> maxYearSpinner = new Spinner<>();

	private final ListView<String> genreListView = new ListView<>(FXCollections.observableList(db.getGenres()));

	private final TextField artistFilterField = new TextField();
	private final TextField titleFilterField = new TextField();

	private final ToggleGroup toggle = new ToggleGroup();
	private final RadioButton rbCD = new RadioButton("CD");
	private final RadioButton rbLP = new RadioButton("LP");
	private final RadioButton rbBoth = new RadioButton("Both");

	private final ObservableSet<String> selectedGenres = FXCollections.observableSet();

	private final Predicate<Recording> yearFilter = new Predicate<>() {
		@Override
		public boolean test(Recording recording) {
			return recording.getYear() >= minYearSpinner.getValue() && recording.getYear() <= maxYearSpinner.getValue();
		}
	};

	private final Predicate<Recording> typeFilter = new Predicate<>() {
		@Override
		public boolean test(Recording recording) {
			var selected = toggle.getSelectedToggle();

			if (selected == null)
				return true;
			else if (selected.equals(rbCD))
				return recording.getType().equals("CD");
			else if (selected.equals(rbLP))
				return recording.getType().equals("LP");
			else
				return true;
		}
	};

	private final Predicate<Recording> artistFilter = new Predicate<>() {
		@Override
		public boolean test(Recording recording) {
			var filterText = artistFilterField.getText();
			if (filterText == null ||filterText.isEmpty()) {
				return true; // no filter applied
			}
			return recording.getArtist().toLowerCase().startsWith(filterText.toLowerCase());
		}
	};


	private final Predicate<Recording> titleFilter = new Predicate<>() {
		@Override
		public boolean test(Recording recording) {

			var value = titleFilterField.getText();

			if (value == null || value.isEmpty()) {
				return true;
			}
			return recording.getTitle().toUpperCase().startsWith(value.toUpperCase());
		}
	};

	private final Predicate<Recording> genreFilter = new Predicate<>() {
		@Override
		public boolean test(Recording recording) {
			if (selectedGenres.isEmpty())
				return true;
			for (String selectedGenre : selectedGenres) {
				if (recording.getGenre().contains(selectedGenre))
					return true;
			}
			return false;
		}
	};

	@Override
	public void start(Stage primaryStage) {

		rbCD.setId("rbCD");
		rbLP.setId("rbLP");
		rbBoth.setId("rbBoth");

		artistFilterField.textProperty().addListener((observable, oldValue, newValue) -> updateFilters());
		artistFilterField.setId("artistField");
		titleFilterField.textProperty().addListener((observable, oldValue, newValue) -> updateFilters());
		titleFilterField.setId("titleField");

		minYearSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1950, 2024, 1950));
		minYearSpinner.valueProperty().addListener((observableValue, oldValue, newValue) -> {
			if(minYearSpinner.getValue() > maxYearSpinner.getValue())
				maxYearSpinner.getValueFactory().setValue(minYearSpinner.getValue());
			updateFilters();
		});
		minYearSpinner.setId("minSpinner");
		maxYearSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1950, 2024, 2024));
		maxYearSpinner.valueProperty().addListener((observableValue, oldValue, newValue) -> {
			if(maxYearSpinner.getValue() < minYearSpinner.getValue())
				minYearSpinner.getValueFactory().setValue(maxYearSpinner.getValue());
			updateFilters();
		});
		maxYearSpinner.setId("maxSpinner");

		table.setId("table");
		//table.setItems(filteredList);

		TableColumn<Recording, String> titleC = new TableColumn<>("Title");
		titleC.setCellValueFactory(new PropertyValueFactory<>("title"));
		TableColumn<Recording, String> artistC = new TableColumn<>("Artist");
		artistC.setCellValueFactory(new PropertyValueFactory<>("artist"));
		TableColumn<Recording, Integer> yearC = new TableColumn<>("Year");
		yearC.setCellValueFactory(new PropertyValueFactory<>("year"));
		TableColumn<Recording, String> typeC = new TableColumn<>("Type");
		typeC.setCellValueFactory(new PropertyValueFactory<>("type"));
		TableColumn<Recording, Set<String>> genreC = new TableColumn<>("Genre");
		genreC.setCellValueFactory(new PropertyValueFactory<>("genre"));

		table.getColumns().addAll(artistC, titleC, yearC, typeC, genreC);



		Button resetButton = new Button("Reset filters");
		resetButton.setId("resetButton");
		resetButton.setOnAction(event -> {
			filteredList.setPredicate(null);
			artistFilterField.clear();
			titleFilterField.clear();
			toggle.selectToggle(rbBoth);
			minYearSpinner.getValueFactory().setValue(1950);
			maxYearSpinner.getValueFactory().setValue(2024);
			filterPane.setExpanded(false);
			selectedGenres.clear();
			genreListView.getSelectionModel().clearSelection();
		});

		toggle.getToggles().addAll(rbCD, rbLP, rbBoth);
		toggle.selectedToggleProperty().addListener((observable, oldValue, newValue) -> updateFilters());
		toggle.selectToggle(rbBoth);
		rbBoth.setSelected(true);

		genreListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		genreListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<String>) c -> {
			selectedGenres.clear();
			selectedGenres.addAll(genreListView.getSelectionModel().getSelectedItems());
			updateFilters();
		});

		MyVBox typeBox = new MyVBox(new Label("Type filter:"), new HBox(20, rbCD, rbLP, rbBoth));
		MyVBox yearBox = new MyVBox(new Label("Year filter:"), new HBox(new MyVBox(new Label("From:")), new MyVBox(minYearSpinner)), new HBox(new MyVBox(new Label("To:")), new MyVBox(maxYearSpinner)));
		MyVBox artistBox = new MyVBox(new Label("Artist filter:"), artistFilterField);
		MyVBox genreBox = new MyVBox(new Label("Genre filter:"), genreListView);
		MyVBox titleBox = new MyVBox(new Label("Title filter:"), titleFilterField);

		filters.add(artistBox, 0, 0);
		filters.add(titleBox, 0, 1);
		filters.add(typeBox, 0, 2);
		filters.add(yearBox, 0, 3);
		filters.add(resetButton, 0, 4);
		filters.add(genreBox, 1, 0, 2, 5);

		filters.setPadding(new Insets(10));
		filters.setHgap(20);
		filters.setVgap(20);

		filterPane.setExpanded(false);
		filterPane.setId("filters");

		Label title = new Label("Record Collection");
		title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
		FlowPane fp = new FlowPane(title);
		fp.setAlignment(Pos.CENTER);

		BorderPane bp = new BorderPane();
		bp.setTop(fp);
		bp.setCenter(table);

		bp.setBottom(filterPane);

		Scene scene = new Scene(bp);
		primaryStage.setWidth(1200);
		primaryStage.setHeight(800);
		primaryStage.setTitle("Record Collection");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void updateFilters() {
		filteredList.setPredicate(artistFilter.and(titleFilter).and(yearFilter).and(typeFilter).and(genreFilter));
		//System.out.println("Filtered List: " + filteredList);
		//System.out.println(filteredList.size());
	}

	static class MyVBox extends VBox {

		MyVBox(Node... items) {
			super(items);
			setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
			setPadding(new Insets(10));
			setSpacing(20);
			setAlignment(Pos.CENTER);
		}
	}
}