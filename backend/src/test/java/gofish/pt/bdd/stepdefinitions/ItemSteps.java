package gofish.pt.bdd.stepdefinitions;

import gofish.pt.dto.ItemUpdateDTO;
import gofish.pt.entity.Category;
import gofish.pt.entity.Item;
import gofish.pt.entity.Material;
import gofish.pt.entity.User;
import gofish.pt.entity.UserRole;
import gofish.pt.entity.UserStatus;
import gofish.pt.repository.ItemRepository;
import gofish.pt.repository.UserRepository;
import gofish.pt.repository.UserRoleRepository;
import gofish.pt.repository.UserStatusRepository;
import gofish.pt.service.ItemService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemSteps {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserStatusRepository userStatusRepository;

    private Item createdItem;
    private Item updatedItem;
    private List<Item> foundItems;
    private Exception itemException;
    private Map<String, User> userMap = new HashMap<>();
    private Map<String, Item> itemMap = new HashMap<>();

    @Before
    public void setUp() {
        createdItem = null;
        updatedItem = null;
        foundItems = null;
        itemException = null;
        userMap.clear();
        itemMap.clear();
    }

    @Given("a user {string} exists")
    public void a_user_exists(String email) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername("User");
            newUser.setEmail(email);
            newUser.setPassword("encodedpassword");
            newUser.setLocation("Test Location");
            newUser.setBalance(0.0);
            User savedUser = userRepository.save(newUser);

            userRoleRepository.save(new UserRole(savedUser.getId(), UserRole.ROLE_USER));
            userStatusRepository.save(new UserStatus(savedUser.getId(), UserStatus.STATUS_ACTIVE));
            return savedUser;
        });
        userMap.put(email, user);
    }

    @When("{string} creates an item with name {string} description {string} and price {double}")
    public void user_creates_an_item_with_name_description_and_price(String email, String name, String description,
            Double price) {
        User owner = userMap.get(email);

        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setCategory(Category.RODS);
        item.setMaterial(Material.GRAPHITE);
        item.setOwner(owner);
        item.setAvailable(true);
        item.setActive(true);

        createdItem = itemRepository.save(item);
        itemMap.put(name, createdItem);
    }

    @Then("the item should be created successfully")
    public void the_item_should_be_created_successfully() {
        assertThat(createdItem).isNotNull();
        assertThat(createdItem.getId()).isNotNull();
    }

    @Then("the item owner should be {string}")
    public void the_item_owner_should_be(String email) {
        assertThat(createdItem.getOwner()).isNotNull();
        assertThat(createdItem.getOwner().getEmail()).isEqualTo(email);
    }

    @Given("an item {string} exists owned by {string}")
    public void an_item_exists_owned_by(String itemName, String email) {
        User owner = userMap.get(email);

        Item item = new Item();
        item.setName(itemName);
        item.setDescription("Test Description");
        item.setPrice(100.0);
        item.setCategory(Category.RODS);
        item.setMaterial(Material.GRAPHITE);
        item.setOwner(owner);
        item.setAvailable(true);
        item.setActive(true);

        createdItem = itemRepository.save(item);
        itemMap.put(itemName, createdItem);
    }

    @When("{string} updates the item with name {string}")
    public void user_updates_the_item_with_name(String email, String newName) {
        User user = userMap.get(email);
        ItemUpdateDTO updateDTO = new ItemUpdateDTO();
        updateDTO.setName(newName);

        try {
            updatedItem = itemService.updateItem(createdItem.getId(), updateDTO, user.getId());
        } catch (ResponseStatusException e) {
            itemException = e;
        }
    }

    @Then("the item name should be {string}")
    public void the_item_name_should_be(String expectedName) {
        assertThat(itemException).isNull();
        assertThat(updatedItem).isNotNull();
        assertThat(updatedItem.getName()).isEqualTo(expectedName);
    }

    @When("{string} attempts to update the item")
    public void user_attempts_to_update_the_item(String email) {
        User user = userMap.get(email);
        ItemUpdateDTO updateDTO = new ItemUpdateDTO();
        updateDTO.setName("Unauthorized Update");

        try {
            updatedItem = itemService.updateItem(createdItem.getId(), updateDTO, user.getId());
        } catch (ResponseStatusException e) {
            itemException = e;
        }
    }

    @Then("the update should fail with a forbidden error")
    public void the_update_should_fail_with_a_forbidden_error() {
        assertThat(itemException).isNotNull();
        assertThat(itemException).isInstanceOf(ResponseStatusException.class);
        ResponseStatusException rse = (ResponseStatusException) itemException;
        assertThat(rse.getStatusCode().value()).isEqualTo(403);
    }

    @When("I search for items owned by {string}")
    public void i_search_for_items_owned_by(String email) {
        User user = userMap.get(email);
        foundItems = itemService.findByOwnerId(user.getId());
    }

    @Then("I should receive items owned by {string}")
    public void i_should_receive_items_owned_by(String email) {
        assertThat(foundItems).isNotNull();
        assertThat(foundItems).isNotEmpty();
        assertThat(foundItems).allMatch(item -> item.getOwner().getEmail().equals(email));
    }
}
