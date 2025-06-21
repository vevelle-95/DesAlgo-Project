import React from "react";
import { View, TouchableOpacity, Text, StyleSheet } from "react-native";

const modes = ["driving", "walking", "transit"] as const;

type ModeTabsProps = {
  selectedMode: string;
  onSelectMode: (mode: "driving" | "walking" | "transit") => void;
};

export default function ModeTabs({
  selectedMode,
  onSelectMode,
}: ModeTabsProps) {
  return (
    <View style={styles.container}>
      {modes.map((mode) => (
        <TouchableOpacity
          key={mode}
          style={[styles.tab, selectedMode === mode && styles.activeTab]}
          onPress={() => onSelectMode(mode)}
        >
          <Text style={styles.tabText}>{mode}</Text>
        </TouchableOpacity>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: "row",
    justifyContent: "space-around",
    backgroundColor: "#ddd",
    padding: 10,
  },
  tab: {
    padding: 10,
    borderRadius: 5,
    backgroundColor: "#bbb",
  },
  activeTab: {
    backgroundColor: "#007bff",
  },
  tabText: {
    color: "white",
  },
});
