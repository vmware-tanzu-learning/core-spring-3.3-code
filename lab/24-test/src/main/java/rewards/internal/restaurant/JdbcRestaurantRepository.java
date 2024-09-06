package rewards.internal.restaurant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import common.money.Percentage;

/**
 * Loads restaurants from a data source using the JDBC API.
 */
@Repository
public class JdbcRestaurantRepository implements RestaurantRepository {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private DataSource dataSource;

	/**
	 * The Restaurant object cache. Cached restaurants are indexed by their merchant numbers.
	 */
	private Map<String, Restaurant> restaurantCache;

	/**
	 * Constructor logs creation so we know which repository we are using.
	 */
	public JdbcRestaurantRepository() {
		logger.info("Creating " + getClass().getSimpleName());
	}

	/**
	 * Sets the data source this repository will use to load restaurants.
	 *
	 * @param dataSource the data source
	 */
	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Restaurant findByMerchantNumber(String merchantNumber) {
		return queryRestaurantCache(merchantNumber);
	}

	/**
	 * Helper method that populates the {@link #restaurantCache restaurant object cache} from rows in the T_RESTAURANT
	 * table. Cached restaurants are indexed by their merchant numbers. This method is called on initialization.
	 */
	@PostConstruct
	void populateRestaurantCache() {
		logger.info("Loading restaurant cache");
		restaurantCache = new HashMap<>();
		String sql = "select MERCHANT_NUMBER, NAME, BENEFIT_PERCENTAGE from T_RESTAURANT";
		try (Connection conn = dataSource.getConnection();
			 PreparedStatement ps = conn.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Restaurant restaurant = mapRestaurant(rs);
				// index the restaurant by its merchant number
				restaurantCache.put(restaurant.getNumber(), restaurant);
			}
		} catch (SQLException e) {
			throw new RuntimeException("SQL exception occurred finding by merchant number", e);
		}

		logger.info("Finished loading restaurant cache");
	}

	/**
	 * Helper method that simply queries the cache of restaurants.
	 *
	 * @param merchantNumber the restaurant's merchant number
	 * @return the restaurant
	 * @throws EmptyResultDataAccessException if no restaurant was found with that merchant number
	 */
	private Restaurant queryRestaurantCache(String merchantNumber) {
		Restaurant restaurant = restaurantCache.get(merchantNumber);
		if (restaurant == null) {
			throw new EmptyResultDataAccessException(1);
		}
		return restaurant;
	}

	/**
	 * Helper method that clears the cache of restaurants.  This method is called on destruction
	 */
	@PreDestroy
	void clearRestaurantCache() {
		logger.info("Clearing restaurant cache");
		restaurantCache.clear();
	}

	/**
	 * Maps a row returned from a query of T_RESTAURANT to a Restaurant object.
	 *
	 * @param rs the result set with its cursor positioned at the current row
	 */
	private Restaurant mapRestaurant(ResultSet rs) throws SQLException {
		// get the row column data
		String name = rs.getString("NAME");
		String number = rs.getString("MERCHANT_NUMBER");
		Percentage benefitPercentage = Percentage.valueOf(rs.getString("BENEFIT_PERCENTAGE"));
		// map to the object
		Restaurant restaurant = new Restaurant(number, name);
		restaurant.setBenefitPercentage(benefitPercentage);
		return restaurant;
	}
}