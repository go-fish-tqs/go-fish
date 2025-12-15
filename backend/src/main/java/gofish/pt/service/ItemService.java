package gofish.pt.service;

import gofish.pt.dto.BlockDateRequestDTO;
import gofish.pt.dto.ItemFilter;
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

    public List<Item> findByOwnerId(Long ownerId) {
        return itemRepository.findAllByOwnerId(ownerId);
    }

    public List<Item> findAll(ItemFilter filter) {

        if (filter == null)
            return findAll();

        Specification<Item> spec = Specification.allOf(nameContains(filter.name()),
                categoryIs(filter.category()),
                materialIs(filter.material()),
                priceBetween(filter.minPrice(), filter.maxPrice()),
                activeIs(true));

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
