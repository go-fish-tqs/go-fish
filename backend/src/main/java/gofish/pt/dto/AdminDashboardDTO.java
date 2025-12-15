package gofish.pt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {
    private int activeBookings;
    private int pendingBookings;
    private int totalUsers;
    private int suspendedUsers;
    private int totalItems;
    private int inactiveItems;
    private double totalRevenue;
}
