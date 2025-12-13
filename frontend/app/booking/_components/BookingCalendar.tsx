"use client";

import { useState, useMemo } from "react";

interface BookingCalendarProps {
  itemId: string;
  startDate?: string;
  endDate?: string;
  onStartDateChange?: (date: string) => void;
  onEndDateChange?: (date: string) => void;
  // NOVA PROP: Recebe a lista de datas bloqueadas ["2025-01-01", "2025-01-02"]
  unavailableDates?: string[];
}

export default function BookingCalendar({
  itemId,
  startDate,
  endDate,
  onStartDateChange,
  onEndDateChange,
  unavailableDates = [], // Default a array vazio
}: BookingCalendarProps) {
  const [currentDate, setCurrentDate] = useState(new Date());
  const [selectionMode, setSelectionMode] = useState<"start" | "end">("start");

  const year = currentDate.getFullYear();
  const month = currentDate.getMonth() + 1; // 1-12

  // OTIMIZAÇÃO: Transforma o array em Set para a pesquisa ser instantânea (O(1))
  // Assim não temos de correr o array todo para cada dia do mês
  const blockedDatesSet = useMemo(() => {
    return new Set(unavailableDates);
  }, [unavailableDates]);

  // Cálculos do calendário
  const daysInMonth = new Date(year, month, 0).getDate();
  const firstDayOfMonth = new Date(year, month - 1, 1).getDay();

  const days = useMemo(() => {
    const result: (number | null)[] = [];
    for (let i = 0; i < firstDayOfMonth; i++) {
      result.push(null);
    }
    for (let i = 1; i <= daysInMonth; i++) {
      result.push(i);
    }
    return result;
  }, [daysInMonth, firstDayOfMonth]);

  const formatDate = (day: number) => {
    const m = month.toString().padStart(2, "0");
    const d = day.toString().padStart(2, "0");
    return `${year}-${m}-${d}`;
  };

  // NOVA LÓGICA: Verifica se o dia está no Set de datas bloqueadas
  const isBooked = (day: number) => {
    const dateStr = formatDate(day);
    return blockedDatesSet.has(dateStr);
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
    return new Date(year, month - 1, day) < today;
  };

  const isInRange = (day: number) => {
    if (!startDate || !endDate) return false;
    const dateStr = formatDate(day);
    return dateStr > startDate && dateStr < endDate;
  };

  const isStartDate = (day: number) => formatDate(day) === startDate;
  const isEndDate = (day: number) => formatDate(day) === endDate;

  const handleDayClick = (day: number) => {
    // Bloqueia cliques no passado OU em dias ocupados
    if (isPast(day) || isBooked(day)) return;

    const dateStr = formatDate(day);

    if (selectionMode === "start") {
      onStartDateChange?.(dateStr);
      // Se a nova data de início for depois do fim atual, limpa o fim
      if (endDate && dateStr > endDate) onEndDateChange?.("");
      setSelectionMode("end");
    } else {
      if (startDate && dateStr < startDate) {
        // Se clicou antes do início, assume que é um novo início
        onStartDateChange?.(dateStr);
      } else {
        onEndDateChange?.(dateStr);
        setSelectionMode("start");
      }
    }
  };

  const prevMonth = () => setCurrentDate(new Date(year, month - 2, 1));
  const nextMonth = () => setCurrentDate(new Date(year, month, 1));

  const monthName = currentDate.toLocaleString("default", { month: "long" });
  const weekDays = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

  return (
    <div className="bg-white border border-gray-200 rounded-xl p-3 shadow-md w-full h-full flex flex-col">
      {/* Header */}
      <div className="flex items-center justify-between mb-2">
        <div>
          <h3 className="text-sm font-semibold text-gray-800">Select Dates</h3>
          <p className="text-xs text-gray-600">
            {selectionMode === "start"
              ? "Select start date"
              : "Select end date"}
          </p>
        </div>
        <div className="flex items-center gap-1">
          <button
            onClick={prevMonth}
            className="p-1.5 rounded-lg bg-gray-100 hover:bg-gray-200 transition-all"
          >
            <svg
              className="w-3 h-3 text-gray-700"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M15 19l-7-7 7-7"
              />
            </svg>
          </button>
          <span className="px-2 py-0.5 text-xs font-semibold text-gray-800 min-w-[90px] text-center">
            {monthName} {year}
          </span>
          <button
            onClick={nextMonth}
            className="p-1.5 rounded-lg bg-gray-100 hover:bg-gray-200 transition-all"
          >
            <svg
              className="w-3 h-3 text-gray-700"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 5l7 7-7 7"
              />
            </svg>
          </button>
        </div>
      </div>

      {/* Calendar Grid */}
      <div className="grid grid-cols-7 gap-0.5 flex-1">
        {/* Weekday Headers */}
        {weekDays.map((day, i) => (
          <div
            key={i}
            className="text-center text-[10px] font-semibold text-gray-700 py-1"
          >
            {day}
          </div>
        ))}

        {/* Day Cells */}
        {days.map((day, index) => {
          if (day === null) {
            return <div key={index} className="aspect-square" />;
          }

          const past = isPast(day);
          const booked = isBooked(day); // Agora usa a prop unavailableDates
          const today = isToday(day);
          const inRange = isInRange(day);
          const isStart = isStartDate(day);
          const isEnd = isEndDate(day);

          // Só é clicável se não for passado e não estiver reservado
          const clickable =
            !past && !booked && (onStartDateChange || onEndDateChange);

          let cellClass =
            "aspect-square flex items-center justify-center text-xs font-medium rounded transition-all ";

          if (isStart) {
            cellClass += "bg-blue-600 text-white";
          } else if (isEnd) {
            cellClass += "bg-indigo-600 text-white";
          } else if (inRange) {
            cellClass += "bg-blue-100 text-blue-800";
          } else if (booked) {
            cellClass += "bg-red-500 text-white cursor-not-allowed opacity-60"; // Vermelho para ocupado
          } else if (past) {
            cellClass += "text-gray-300 cursor-not-allowed";
          } else {
            cellClass +=
              "text-gray-800 bg-gray-50 hover:bg-blue-50 cursor-pointer";
          }

          if (today) {
            cellClass += " ring-1 ring-blue-500 ring-offset-1";
          }

          return (
            <button
              key={index}
              onClick={() => handleDayClick(day)}
              disabled={past || booked || !clickable}
              className={cellClass}
            >
              {day}
            </button>
          );
        })}
      </div>

      {/* Legend */}
      <div className="flex items-center gap-3 mt-2 pt-2 border-t border-gray-200 text-[10px]">
        <div className="flex items-center gap-1">
          <div className="w-2 h-2 rounded bg-red-500 opacity-60"></div>
          <span className="text-gray-700">Booked</span>
        </div>
        <div className="flex items-center gap-1">
          <div className="w-2 h-2 rounded bg-blue-600"></div>
          <span className="text-gray-700">Start</span>
        </div>
        <div className="flex items-center gap-1">
          <div className="w-2 h-2 rounded bg-indigo-600"></div>
          <span className="text-gray-700">End</span>
        </div>
      </div>
    </div>
  );
}
