import React, { useEffect, useRef, useState, Suspense } from "react";
import { View, Text, StyleSheet, SafeAreaView, Platform } from "react-native";
import SearchBar from "../../components/ui/SearchBar";
import ModeTabs from "../../components/ui/ModeTabs";
import RouteCard from "../../components/ui/RouteCard";

const MAPBOX_TOKEN =
  "pk.eyJ1IjoiaXZlbGxlOTUiLCJhIjoiY21jNmdvejR2MHE0aDJrc2Npbmd6b3VjNyJ9.AQxQ8n_1ZM6S2JUL4ELccA";

let mapboxgl: any;
if (Platform.OS === "web") {
  mapboxgl = require("mapbox-gl");
  mapboxgl.accessToken = MAPBOX_TOKEN;
}

const NativeMap =
  Platform.OS !== "web"
    ? React.lazy(() => import("../../components/ui/NativeMap"))
    : () => null;

export default function ExploreScreen() {
  const [origin, setOrigin] = useState({
    latitude: 14.5995,
    longitude: 120.9842,
  });
  const [destination, setDestination] = useState({
    latitude: 14.5086,
    longitude: 121.0198,
  });
  const [selectedMode, setSelectedMode] = useState<
    "driving" | "walking" | "transit"
  >("driving");

  const mapRef = useRef(null);

  useEffect(() => {
    if (!MAPBOX_TOKEN) {
      console.error("Missing Mapbox access token.");
      return;
    }

    if (Platform.OS === "web" && mapRef.current && mapboxgl) {
      const map = new mapboxgl.Map({
        container: mapRef.current,
        style: "mapbox://styles/mapbox/streets-v11",
        center: [origin.longitude, origin.latitude],
        zoom: 10,
      });

      new mapboxgl.Marker()
        .setLngLat([origin.longitude, origin.latitude])
        .addTo(map);

      new mapboxgl.Marker()
        .setLngLat([destination.longitude, destination.latitude])
        .addTo(map);
    }
  }, [origin, destination]);

  return (
    <SafeAreaView style={styles.container}>
      <SearchBar
        onSelectOrigin={setOrigin}
        onSelectDestination={setDestination}
      />

      {Platform.OS === "web" ? (
        <View ref={mapRef} style={styles.webMapContainer} />
      ) : (
        <Suspense fallback={<Text>Loading native map...</Text>}>
          <NativeMap origin={origin} destination={destination} />
        </Suspense>
      )}

      <ModeTabs selectedMode={selectedMode} onSelectMode={setSelectedMode} />
      <RouteCard
        mode={selectedMode}
        duration="49 min"
        cost="$8.40"
        traffic="Moderate"
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
  },
  webMapContainer: {
    flex: 1,
    minHeight: 300,
  },
});
