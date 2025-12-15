package gofish.pt.service;

import gofish.pt.dto.BlockDateRequestDTO;
import gofish.pt.dto.ItemFilter;
import gofish.pt.dto.ItemUpdateDTO;
import gofish.pt.entity.*;
import gofish.pt.dto.ItemDTO;
import gofish.pt.mapper.ItemMapper;
import gofish.pt.repository.BlockedDateRepository;
import gofish.pt.repository.BookingRepository;
import gofish.pt.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

import static gofish.pt.repository.ItemSpecifications.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final BlockedDateRepository blockedDateRepository;
    private final BookingRepository bookingRepository;
    private final ItemMapper itemMapper;

    public Optional<Item> findById(long id) {
        return itemRepository.findById(id);
    }

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public List<Item> findAll(ItemFilter filter) {

        if (filter == null)
            return findAll();

        Specification<Item> spec = Specification.allOf(nameContains(filter.name()),
                categoryIs(filter.category()),
                materialIs(filter.material()),
                priceBetween(filter.minPrice(), filter.maxPrice()));

        String sortBy = (filter.sortBy() != null) ? filter.sortBy() : "id";
        Sort.Direction direction = (filter.direction() != null) ? filter.direction() : Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortBy);

        return itemRepository.findAll(spec, sort);
    }

    public Item save(ItemDTO dto) {
        Item item = itemMapper.toEntity(dto);

        if (item == null)
            return null;
        return itemRepository.save(item);
    }

    /**
     * Updates an existing item with partial data
     * Only the owner can update the item
     * Price changes do not affect existing bookings
     * 
     * @param itemId The ID of the item to update
     * @param updateDTO The update data (all fields optional)
     * @param ownerId The ID of the user attempting the update
     * @return The updated item
     * @throws ResponseStatusException 404 if item not found, 403 if not owner
     */
    public Item updateItem(Long itemId, ItemUpdateDTO updateDTO, Long ownerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        System.out.println("=== UPDATE ITEM SERVICE DEBUG ===");
        System.out.println("Item owner: " + item.getOwner());
        System.out.println("Item owner ID: " + (item.getOwner() != null ? item.getOwner().getId() : "null"));
        System.out.println("Requesting user ID: " + ownerId);
        System.out.println("Are they equal? " + (item.getOwner() != null && item.getOwner().getId().equals(ownerId)));

        // Authorization: Only owner can update
        if (item.getOwner() == null || !item.getOwner().getId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the item owner can update this item");
        }

        // Apply partial updates - only update fields that are provided
        if (updateDTO.getName() != null) {
            item.setName(updateDTO.getName());
        }

        if (updateDTO.getDescription() != null) {
            item.setDescription(updateDTO.getDescription());
        }

        if (updateDTO.getPhotoUrls() != null) {
            item.setPhotoUrls(updateDTO.getPhotoUrls());
        }

        if (updateDTO.getCategory() != null) {
            item.setCategory(updateDTO.getCategory());
        }

        if (updateDTO.getMaterial() != null) {
            item.setMaterial(updateDTO.getMaterial());
        }

        if (updateDTO.getPrice() != null) {
            // Price changes do not affect existing bookings
            // They already have their price locked in
            item.setPrice(updateDTO.getPrice());
        }

        if (updateDTO.getAvailable() != null) {
            item.setAvailable(updateDTO.getAvailable());
        }

        return itemRepository.save(item);
    }

    public void delete(Item item) {
        if (item != null)
            itemRepository.delete(item);
    }

    public boolean exists(long id) {
        return itemRepository.existsById(id);
    }

    public List<Category> getCategories() {
        return Arrays.stream(Category.values())
                .filter(Category::isTopLevel)
                .toList();
    }

    public Map<Material.MaterialGroup, List<Material>> getMaterials() {
        Map<Material.MaterialGroup, List<Material>> materials = new HashMap<>();
        for (Material.MaterialGroup group : Material.MaterialGroup.values()) {
            materials.put(group, group.getMaterials());
        }
        return materials;
    }

    public BlockedDate blockDateRange(Long itemId, BlockDateRequestDTO request, Long ownerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        // Authorization: Check if the user is the owner
        if (item.getOwner() == null || !item.getOwner().getId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the item owner can block dates");
        }

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        // Validate date range
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date cannot be after end date");
        }

        // Conflict Check: Cannot block dates with existing confirmed bookings
        boolean hasConflict = bookingRepository.existsOverlappingBooking(
                itemId,
                startDate,
                endDate);

        if (hasConflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The requested date range conflicts with an existing confirmed booking");
        }

        BlockedDate blockedDate = new BlockedDate(startDate, endDate, request.getReason(), item);
        return blockedDateRepository.save(blockedDate);
    }

    public List<BlockedDate> getBlockedDates(Long itemId, LocalDate from, LocalDate to) {
        return blockedDateRepository.findBlockedDatesInRange(itemId, from, to);
    }

    public void unblockDateRange(Long blockedDateId, Long ownerId) {
        BlockedDate blockedDate = blockedDateRepository.findById(blockedDateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blocked date period not found"));

        // Authorization: Check if the user is the owner of the item associated with the
        // blocked date
        if (blockedDate.getItem().getOwner() == null || !blockedDate.getItem().getOwner().getId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the item owner can remove a blocked date period");
        }

        blockedDateRepository.delete(blockedDate);
    }
}
