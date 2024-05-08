import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.matcher.control.TableViewMatchers;
import org.testfx.service.query.NodeQuery;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class Exercise5Test {
	//
	private static final String UPDATED_DATE = "2024-03-27";
	//
	private static final String RESET_BUTTON = "#resetButton";
	private static final String TABLE = "#table";
	private static final String FILTER_PANE = "#filters";
	private static final String MAX_SPINNER = "#maxSpinner";
	private static final String MIN_SPINNER = "#minSpinner";
	private static final String ARTIST_FIELD = "#artistField";
	private static final String TITLE_FIELD = "#titleField";
	private static final String RB_BOTH = "#rbBoth";
	private static final String RB_CD = "#rbCD";
	private static final String RB_LP = "#rbLP";
	private static final int UNFILTERED_COUNT = 504;

	private static final int MAX_SPINNER_MAX_YEAR = 2024;

	private static final int MIN_SPINNER_MIN_YEAR = 1950;
	//
	private static final Object[] DANGELO_VOODOO = {"D'Angelo", "Voodoo", 2000, "CD", "[Funk, Hip Hop, Soul]"};
	private static final Object[] KANYE_DARK_FANTASY = {"Kanye West", "My Beautiful Dark Twisted Fantasy", 2010, "CD", "[Hip Hop]"};
	private static final Object[] BEACH_BOYS_SMILE = {"The Beach Boys", "The Smile Sessions", 2011, "LP", "[Rock]"};
	private static final Object[] VIAGRA_BOYS_STREET_WORMS = {"Viagra Boys", "Street Worms", 2018, "CD", "[Rock]"};
	private static final Object[] VIAGRA_BOYS_WELFARE_JAZZ = {"Viagra Boys", "Welfare Jazz", 2021, "LP", "[Rock]"};
	private static final Object[] VIAGRA_BOYS_CAVE_WORLD = {"Viagra Boys", "Cave World", 2022, "CD", "[Rock]"};
	private static final Object[] IDLES_TANGK = {"IDLES", "TANGK", 2024, "CD", "[Rock]"};
	//
	private Stage stage;
	private FilteredList<Recording> filteredList;
	private FxRobot robot;
	private Exercise5 app;
	private boolean allNamedControlsFound;
	private NodeQuery table;
	private int lastSuccessfulStep = -1;

	@BeforeAll
	static void beforeAll() {
		Properties properties = System.getProperties();
		if (properties.get("os.name").equals("Mac OS X")) {
			System.setProperty("java.awt.headless", "false");
			System.setProperty("testfx.headless", "false");
		} else {
			System.setProperty("java.awt.headless", "true");
			System.setProperty("testfx.headless", "true");
		}
		System.setProperty("testfx.robot", "glass");
		System.setProperty("prism.order", "sw");
		System.setProperty("prism.text", "t2k");
		System.setProperty("headless.geometry", "1600x1200-32");
	}

	@BeforeEach
	public void setupClass(FxRobot robot) throws Exception {

		this.stage = FxToolkit.registerPrimaryStage();

		this.app = (Exercise5) FxToolkit.setupApplication(Exercise5.class);
		assertNotNull(this.app, "Fel: kunde inte starta applikationen.");

		this.filteredList = getFilteredListUsingReflection();
		assertNotNull(this.filteredList, "Fel: kunde inte hitta medlemsvariabeln för FilteredList.");

		this.robot = robot;
		assertNotNull(this.robot, "Fel: internt fel i testramverket.");

		this.table = robot.lookup(TABLE);
	}

	@AfterAll
	void tearDown() throws TimeoutException {
		FxToolkit.cleanupStages();
	}

	private void _decrementSpinner(Spinner<Integer> spinner, int steps) {
		spinner.getValueFactory().decrement(steps);
	}

	private void _incrementSpinner(Spinner<Integer> spinner, int steps) {
		spinner.getValueFactory().increment(steps);
	}

	private void _setSpinnerValue(Spinner<Integer> spinner, int value) {
		spinner.getValueFactory().setValue(value);
	}

	private void _testFilterAndReset(Consumer<Void> function) {
		assertEquals(UNFILTERED_COUNT, filteredList.size());

		robot.clickOn(FILTER_PANE);

		function.accept(null);

		robot.clickOn(RESET_BUTTON);
	}

	private void _validateArtistTextField(String fieldName, String query, Object[] expectedRow, int expectedCount) {
		_validateTextField(fieldName, query, expectedRow, expectedCount, "artist");
	}

	private void _validateTextField(String fieldName, String query, Object[] expectedRow, int expectedCount, String field) {
		var textField = (TextField) robot.lookup(fieldName).query();
		textField.clear();
		robot.clickOn(fieldName);
		robot.write(query);

		var baseMessage = "Sökning på %s gav för %s antal träffar.\n";
		if (expectedCount > filteredList.size())
			assertEquals(expectedCount, filteredList.size(), String.format(baseMessage, field, "litet"));
		else if (expectedCount < filteredList.size())
			assertEquals(expectedCount, filteredList.size(), String.format(baseMessage, field, "stort"));

		var matcher = expectedCount > 0 ? TableViewMatchers.containsRow(expectedRow) : not(TableViewMatchers.containsRow(expectedRow));

		FxAssert.verifyThat(table, matcher);
	}

	private void _validateTitleTextField(String fieldName, String query, Object[] expectedRow, int expectedCount) {
		_validateTextField(fieldName, query, expectedRow, expectedCount, "titel");
	}

	@SuppressWarnings("unchecked")
	private FilteredList<Recording> getFilteredListUsingReflection() throws IllegalAccessException {

		for (Field declaredField : Exercise5.class.getDeclaredFields()) {
			if (declaredField.getType().isAssignableFrom(FilteredList.class)) {
				if (declaredField.getType().equals(FilteredList.class)) {
					declaredField.setAccessible(true);
					return (FilteredList<Recording>) declaredField.get(app);
				}
			}
		}

		return null;
	}

	@Test
	@Order(0)
	@DisplayName("Information")
	void __version() {
		System.out.printf("Test uppdaterat %s%n", UPDATED_DATE);
		lastSuccessfulStep = 0;
	}

	@Test
	@DisplayName("Verifiera att kontroller har id satta med setId.")
	@Order(5)
	void test00_hasCorrectlyNamedControls() {
		Assumptions.assumeTrue(lastSuccessfulStep == 0);

		var controls = Map.of(
				RESET_BUTTON, "Knappen reset"
				, TABLE, "Tabellen (TableView)"
				, FILTER_PANE, "Panelen med filter"
				, MAX_SPINNER, "Spinner för högsta år"
				, MIN_SPINNER, "Spinner för lägsta år"
				, ARTIST_FIELD, "Textinmatningsfältet för artist"
				, TITLE_FIELD, "Textinmatningsfältet för titel"
				, RB_BOTH, "Radioknapp för både CD & LP"
				, RB_CD, "Radioknapp för CD"
				, RB_LP, "Radioknapp för LP"
		);

		for (var entry : controls.entrySet()) {
			var node = robot.lookup(entry.getKey()).tryQuery();
			assertTrue(node.isPresent(), String.format("%s kunde inte hittas. Den ska ha id '%s' satt med setId.", entry.getValue(), entry.getKey().substring(1)));
		}

		allNamedControlsFound = true;
		lastSuccessfulStep = 5;
	}

	@Test
	@Order(10)
	@DisplayName("Test av filter: artist")
	void testArtist() {
		Assumptions.assumeTrue(allNamedControlsFound, "Testet avbröts eftersom alla namngivna kontroller inte kunde hittas.");
		Assumptions.assumeTrue(lastSuccessfulStep == 5, "Avbröt eftersom föregående steg misslyckades.");

		_testFilterAndReset(unused -> {
			_validateArtistTextField(ARTIST_FIELD, "D", DANGELO_VOODOO, 19);
			_validateArtistTextField(ARTIST_FIELD, "D'", DANGELO_VOODOO, 1);

			_validateArtistTextField(ARTIST_FIELD, "I", IDLES_TANGK, 3);
			_validateArtistTextField(ARTIST_FIELD, "Id", IDLES_TANGK, 1);

			_validateArtistTextField(ARTIST_FIELD, "V", VIAGRA_BOYS_STREET_WORMS, 14);
			_validateArtistTextField(ARTIST_FIELD, "VI", VIAGRA_BOYS_WELFARE_JAZZ, 3);
			_validateArtistTextField(ARTIST_FIELD, "VIa", VIAGRA_BOYS_CAVE_WORLD, 3);
		});

		lastSuccessfulStep = 10;
	}

	@Test
	@Order(20)
	@DisplayName("Test av filter: titel")
	void testTitle() {
		Assumptions.assumeTrue(allNamedControlsFound, "Testet avbröts eftersom alla namngivna kontroller inte kunde hittas.");
		Assumptions.assumeTrue(lastSuccessfulStep == 10, "Avbröt eftersom föregående steg misslyckades.");

		_testFilterAndReset(unused -> {
			_validateTitleTextField(TITLE_FIELD, "Street Worms", VIAGRA_BOYS_STREET_WORMS, 1);
			_validateTitleTextField(TITLE_FIELD, "Welfare Jazz", VIAGRA_BOYS_WELFARE_JAZZ, 1);
			_validateTitleTextField(TITLE_FIELD, "Cave World", VIAGRA_BOYS_CAVE_WORLD, 1);

			_validateTitleTextField(TITLE_FIELD, "TANGK", IDLES_TANGK, 1);
			_validateTitleTextField(TITLE_FIELD, "TANGKX", IDLES_TANGK, 0);
		});

		lastSuccessfulStep = 20;
	}

	@Test
	@Order(30)
	@DisplayName("Test av filter: typ")
	void testType() {
		Assumptions.assumeTrue(allNamedControlsFound, "Testet avbröts eftersom alla namngivna kontroller inte kunde hittas.");
		Assumptions.assumeTrue(lastSuccessfulStep == 20, "Avbröt eftersom föregående steg misslyckades.");

		var hasCd = TableViewMatchers.hasTableCell("CD");
		var hasLp = TableViewMatchers.hasTableCell("LP");
		var hasBothCdAndLp = allOf(hasCd, hasLp);

    	_testFilterAndReset(unused -> {
			robot.clickOn(RB_BOTH);
			robot.clickOn(RB_CD);
			assertFalse(hasLp.matches(table));
			robot.clickOn(RB_LP);

			assertFalse(hasCd.matches(table));
			robot.clickOn(RB_BOTH);
			assertEquals(UNFILTERED_COUNT, filteredList.size(), "Fel: tabellen är fortfarande filtrerad efter klick på reset.");
			FxAssert.verifyThat(table, hasBothCdAndLp);
		});

		lastSuccessfulStep = 30;
	}

	@SuppressWarnings("unchecked")
	@Test
	@Order(40)
	@DisplayName("Test av filter: år")
	void testYear() {
		Assumptions.assumeTrue(allNamedControlsFound, "Testet avbröts eftersom alla namngivna kontroller inte kunde hittas.");
		Assumptions.assumeTrue(lastSuccessfulStep == 30, "Avbröt eftersom föregående steg misslyckades.");

		_testFilterAndReset(unused -> {
			var minSpinner = (Spinner<Integer>) robot.lookup(MIN_SPINNER).query();
			var maxSpinner = (Spinner<Integer>) robot.lookup(MAX_SPINNER).query();

			var maxSpinnerValueFactory = (SpinnerValueFactory.IntegerSpinnerValueFactory) maxSpinner.getValueFactory();
			assertEquals(MAX_SPINNER_MAX_YEAR, maxSpinnerValueFactory.getMax(),
					String.format("Det högsta värdet för spinnern för högst år är fel. Det borde vara: %d, men var: %d%n",
							MAX_SPINNER_MAX_YEAR, maxSpinnerValueFactory.getMax()));

			var minSpinnerValueFactory = (SpinnerValueFactory.IntegerSpinnerValueFactory) minSpinner.getValueFactory();
			assertEquals(MIN_SPINNER_MIN_YEAR, minSpinnerValueFactory.getMin(),
					String.format("Det lägsta värdet för spinnern för lägst år är fel. Det borde vara: %d, men var: %d%n",
							MIN_SPINNER_MIN_YEAR, minSpinnerValueFactory.getMin()));

			_setSpinnerValue(minSpinner, 2010);
			_setSpinnerValue(maxSpinner, 2024);

			assertEquals(6, filteredList.size());

			FxAssert.verifyThat(table, TableViewMatchers.containsRow(KANYE_DARK_FANTASY));
			FxAssert.verifyThat(table, TableViewMatchers.containsRow(BEACH_BOYS_SMILE));
			FxAssert.verifyThat(table, TableViewMatchers.containsRow(VIAGRA_BOYS_STREET_WORMS));
			FxAssert.verifyThat(table, TableViewMatchers.containsRow(VIAGRA_BOYS_WELFARE_JAZZ));
			FxAssert.verifyThat(table, TableViewMatchers.containsRow(VIAGRA_BOYS_CAVE_WORLD));
			FxAssert.verifyThat(table, TableViewMatchers.containsRow(IDLES_TANGK));

			_setSpinnerValue(minSpinner, 2022);
			_setSpinnerValue(maxSpinner, 2023);
			_decrementSpinner(maxSpinner, 2);

			assertEquals(2021, maxSpinner.getValue(), "Testar att värdet för högsta året är 2021 efter att ha startat på 2023 och sänkts två steg.");
			assertEquals(2021, minSpinner.getValue(), "Testar att värdet för lägsta året är 2021 efter att ha startat på 2022 och den andra spinnern har sänkts två steg från 2023 till 2021.");
			assertEquals(1, filteredList.size(), "Antalet poster i den filtrerade listan stämmer inte.");
			FxAssert.verifyThat(table, TableViewMatchers.containsRow(VIAGRA_BOYS_WELFARE_JAZZ));

			_incrementSpinner(minSpinner, 1);

			assertEquals(2022, minSpinner.getValue(), "Testar att värdet för lägsta året är 2022 efter att ha startat på 2021 och ökats ett steg.");
			assertEquals(2022, maxSpinner.getValue(), "Testar att värdet för högsta året är 2022 efter att ha startat på 2021 och den andra spinnern har ökats ett steg.");
			assertEquals(1, filteredList.size(), "Antalet poster i den filtrerade listan stämmer inte.");
			FxAssert.verifyThat(table, TableViewMatchers.containsRow(VIAGRA_BOYS_CAVE_WORLD));

			robot.clickOn(FILTER_PANE);
		});

		lastSuccessfulStep = 40;
	}

	@Test
	@Order(50)
	@DisplayName("Test av filter: genre")
	void testGenre() {
		Assumptions.assumeTrue(allNamedControlsFound, "Testet avbröts eftersom alla namngivna kontroller inte kunde hittas.");
		Assumptions.assumeTrue(lastSuccessfulStep == 40, "Avbröt eftersom föregående steg misslyckades.");

		_testFilterAndReset(f -> {

			robot.clickOn("Jazz");
			assertEquals(19, filteredList.size());
			assertTrue(filteredList.stream().allMatch(recording -> recording.getGenre().contains("Jazz")));

			robot.clickOn("Rock");
			assertEquals(355, filteredList.size());

			robot.clickOn("Funk");
			assertEquals(80, filteredList.size());
		});

		lastSuccessfulStep = 50;
	}
}
