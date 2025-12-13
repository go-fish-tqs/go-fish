"use client";

import { useQuery } from "@tanstack/react-query";
import { useState, useMemo } from "react";

interface BookingCalendarProps {
    itemId: string;
    startDate?: string;
    endDate?: string;
    onStartDateChange?: (date: string) => void;
    onEndDateChange?: (date: string) => void;
}

interface Booking {
    id: number;
    startDate: string;
    endDate: string;
    status: string;
    userName?: string;
}

export default function BookingCalendar({
    itemId,
    startDate,
    endDate,
    onStartDateChange,
    onEndDateChange,
}: BookingCalendarProps) {
    const [currentDate, setCurrentDate] = useState(new Date());
    const [selectionMode, setSelectionMode] = useState<'start' | 'end'>('start');

    const year = currentDate.getFullYear();
    const month = currentDate.getMonth() + 1;

    const { data: bookings = [], isLoading } = useQuery({
        queryKey: ["bookings", itemId, year, month],
        queryFn: async () => {
            const res = await fetch(
                `${process.env.API_URL}/api/bookings/item/${itemId}/month?year=${year}&month=${month}`
            );
            if (!res.ok) throw new Error("Failed to fetch bookings");
            return res.json() as Promise<Booking[]>;
        },
        enabled: !!itemId,
    });

    const daysInMonth = new Date(year, month, 0).getDate();
    const firstDayOfMonth = new Date(year, month - 1, 1).getDay();

    const days = useMemo(() => {
        const result = [];
        for (let i = 0; i < firstDayOfMonth; i++) {
            result.push(null);
        }
        for (let i = 1; i <= daysInMonth; i++) {
            result.push(i);
        }
        return result;
    }, [daysInMonth, firstDayOfMonth]);

    const formatDate = (day: number) => {
        const m = month.toString().padStart(2, '0');
        const d = day.toString().padStart(2, '0');
        return `${year}-${m}-${d}`;
    };

    const isBooked = (day: number) => {
        const dateToCheck = new Date(year, month - 1, day);
        return bookings.some((booking) => {
            const start = new Date(booking.startDate);
            const end = new Date(booking.endDate);
            start.setHours(0, 0, 0, 0);
            end.setHours(23, 59, 59, 999);
            return dateToCheck >= start && dateToCheck <= end;
        });
    };

    const isToday = (day: number) => {
        const today = new Date();
        return (
            day === today.getDate() &&
            month === today.getMonth() + 1 &&
            year === today.getFullYear()
        );
    };

    const isPast = (day: number) => {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const dateToCheck = new Date(year, month - 1, day);
        return dateToCheck < today;
    };

    const isSelected = (day: number) => {
        const dateStr = formatDate(day);
        return dateStr === startDate || dateStr === endDate;
    };

    const isInRange = (day: number) => {
        if (!startDate || !endDate) return false;
        const dateStr = formatDate(day);
        return dateStr > startDate && dateStr < endDate;
    };

    const isStartDate = (day: number) => formatDate(day) === startDate;
    const isEndDate = (day: number) => formatDate(day) === endDate;

    const handleDayClick = (day: number) => {
        if (isPast(day) || isBooked(day)) return;

        const dateStr = formatDate(day);

        if (selectionMode === 'start') {
            onStartDateChange?.(dateStr);
            // If new start is after current end, clear end
            if (endDate && dateStr > endDate) {
                onEndDateChange?.('');
            }
            setSelectionMode('end');
        } else {
            // If clicked date is before start, make it the new start
            if (startDate && dateStr < startDate) {
                onStartDateChange?.(dateStr);
            } else {
                onEndDateChange?.(dateStr);
                setSelectionMode('start');
            }
        }
    };

    const prevMonth = () => {
        setCurrentDate(new Date(year, month - 2, 1));
    };

    const nextMonth = () => {
        setCurrentDate(new Date(year, month, 1));
    };

    const monthName = currentDate.toLocaleString("default", { month: "long" });
    const weekDays = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

    return (
        <div className="backdrop-blur-xl bg-white/60 border border-white/40 rounded-2xl p-6 shadow-xl">
            {/* Header */}
            <div className="flex items-center justify-between mb-6">
                <div>
                    <h3 className="text-lg font-semibold text-gray-800">Select Dates</h3>
                    <p className="text-sm text-gray-500 mt-1">
                        {selectionMode === 'start' ? 'Click to select start date' : 'Click to select end date'}
                    </p>
                </div>
                <div className="flex items-center gap-2">
                    <button
                        onClick={prevMonth}
                        className="p-2 rounded-xl backdrop-blur bg-white/60 border border-gray-200/50 hover:bg-white/80 transition-all"
                    >
                        <svg className="w-4 h-4 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                        </svg>
                    </button>
                    <span className="px-4 py-2 font-medium text-gray-700 min-w-[140px] text-center">
                        {monthName} {year}
                    </span>
                    <button
                        onClick={nextMonth}
                        className="p-2 rounded-xl backdrop-blur bg-white/60 border border-gray-200/50 hover:bg-white/80 transition-all"
                    >
                        <svg className="w-4 h-4 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                        </svg>
                    </button>
                </div>
            </div>

            {/* Calendar Grid */}
            <div className="grid grid-cols-7 gap-1">
                {weekDays.map((day) => (
                    <div key={day} className="text-center text-xs font-medium text-gray-500 py-2">
                        {day}
                    </div>
                ))}

                {days.map((day, index) => {
                    if (day === null) {
                        return <div key={index} />;
                    }

                    const past = isPast(day);
                    const booked = isBooked(day);
                    const today = isToday(day);
                    const selected = isSelected(day);
                    const inRange = isInRange(day);
                    const isStart = isStartDate(day);
                    const isEnd = isEndDate(day);
                    const clickable = !past && !booked && (onStartDateChange || onEndDateChange);

                    return (
                        <button
                            key={index}
                            onClick={() => handleDayClick(day)}
                            disabled={past || booked || !clickable}
                            className={`
                aspect-square flex items-center justify-center text-sm rounded-xl transition-all relative
                ${today ? "ring-2 ring-blue-400 ring-offset-1" : ""}
                ${past ? "text-gray-300 cursor-not-allowed" : ""}
                ${booked ? "bg-gradient-to-br from-orange-400 to-red-500 text-white font-medium shadow-sm cursor-not-allowed" : ""}
                ${!past && !booked && !selected && !inRange ? "bg-gradient-to-br from-green-50 to-emerald-50 text-gray-700 hover:from-green-100 hover:to-emerald-100 cursor-pointer" : ""}
                ${isStart ? "bg-gradient-to-br from-blue-500 to-blue-600 text-white font-semibold shadow-lg" : ""}
                ${isEnd ? "bg-gradient-to-br from-indigo-500 to-purple-600 text-white font-semibold shadow-lg" : ""}
                ${inRange ? "bg-blue-100 text-blue-700" : ""}
              `}
                        >
                            {day}
                            {isStart && (
                                <span className="absolute -bottom-1 text-[8px] font-medium text-blue-600">START</span>
                            )}
                            {isEnd && (
                                <span className="absolute -bottom-1 text-[8px] font-medium text-purple-600">END</span>
                            )}
                        </button>
                    );
                })}
            </div>

            {/* Legend */}
            <div className="flex flex-wrap items-center gap-4 mt-6 pt-4 border-t border-gray-200/50 text-xs">
                <div className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded bg-gradient-to-br from-green-50 to-emerald-50 border border-green-200"></div>
                    <span className="text-gray-600">Available</span>
                </div>
                <div className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded bg-gradient-to-br from-orange-400 to-red-500"></div>
                    <span className="text-gray-600">Booked</span>
                </div>
                <div className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded bg-gradient-to-br from-blue-500 to-blue-600"></div>
                    <span className="text-gray-600">Start</span>
                </div>
                <div className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded bg-gradient-to-br from-indigo-500 to-purple-600"></div>
                    <span className="text-gray-600">End</span>
                </div>
                {isLoading && (
                    <span className="ml-auto text-gray-400">Loading...</span>
                )}
            </div>
        </div>
    );
}
