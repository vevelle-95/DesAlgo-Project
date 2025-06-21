import React, { useEffect } from "react";
import { View, StyleSheet, PermissionsAndroid, Platform } from "react-native";

let MapboxGL: any;
if (Platform.OS !== "web") {
  MapboxGL = require("@rnmapbox/maps");
  MapboxGL.setAccessToken(
    "pk.eyJ1IjoiaXZlbGxlOTUiLCJhIjoiY21jNmdvejR2MHE0aDJrc2Npbmd6b3VjNyJ9.AQxQ8n_1ZM6S2JUL4ELccA"
  );
}

const MapView = MapboxGL?.MapView as React.ComponentType<any>;
const Camera = MapboxGL?.Camera as React.ComponentType<any>;
const PointAnnotation = MapboxGL?.PointAnnotation as React.ComponentType<any>;

export default function NativeMap({ origin, destination }: any) {
  useEffect(() => {
    if (Platform.OS === "android") {
      PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION
      );
    }
  }, []);

  if (!MapboxGL) return null;

  return (
    <View style={styles.map}>
      <MapView style={styles.map} styleURL={MapboxGL.StyleURL.Street}>
        <Camera
          zoomLevel={10}
          centerCoordinate={[origin.longitude, origin.latitude]}
        />
        <PointAnnotation
          id="origin"
          coordinate={[origin.longitude, origin.latitude]}
        />
        <PointAnnotation
          id="destination"
          coordinate={[destination.longitude, destination.latitude]}
        />
      </MapView>
    </View>
  );
}

const styles = StyleSheet.create({
  map: {
    flex: 1,
  },
});
