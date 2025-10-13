import React from 'react';
import { View, Text, StyleSheet, Dimensions } from 'react-native';
import { LineChart } from 'react-native-chart-kit';
import Colors from '../constants/Colors';

const screenWidth = Dimensions.get('window').width;

export default function GraphComponent({ data }) {
  return (
    <View style={styles.graphContainer}>
      <Text style={styles.graphTitle}>Análise de Dados (Últimos 6 meses)</Text>
      <LineChart
        data={{
          labels: data.labels,
          datasets: [{ data: data.values }],
        }}
        width={screenWidth - 40}
        height={220}
        chartConfig={{
          backgroundColor: Colors.white,
          backgroundGradientFrom: Colors.white,
          backgroundGradientTo: Colors.white,
          decimalPlaces: 0,
          color: (opacity = 1) => `rgba(40, 5, 151, ${opacity})`,
          labelColor: (opacity = 1) => `rgba(51, 51, 51, ${opacity})`,
          propsForDots: {
            r: '6',
            strokeWidth: '2',
            stroke: Colors.primary,
          },
        }}
        bezier
        style={styles.chartStyle}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  graphContainer: {
    backgroundColor: Colors.white,
    borderRadius: 16,
    padding: 10,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 3.84,
    elevation: 5,
    marginBottom: 20,
  },
  graphTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: Colors.text,
    marginBottom: 10,
  },
  chartStyle: {
    borderRadius: 16,
  },
});