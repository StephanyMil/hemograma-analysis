import { Dimensions, StyleSheet, Text, View } from 'react-native';
import { BarChart, PieChart } from 'react-native-chart-kit';
import Colors from '../constants/Colors';

const screenWidth = Dimensions.get('window').width;

const chartConfig = {
  backgroundGradientFrom: Colors.white,
  backgroundGradientTo: Colors.white,
  color: (opacity = 1) => `rgba(40, 5, 151, ${opacity})`,
  strokeWidth: 2,
  barPercentage: 0.5,
  useShadowColorFromDataset: false,
  decimalPlaces: 0,
  labelColor: (opacity = 1) => `rgba(0, 0, 0, ${opacity})`,
};

export const RegionChart = ({ data }) => {
  // Data expected format for PieChart:
  // [ { name: 'Sul', population: 21500000, color: 'rgba(131, 167, 234, 1)', legendFontColor: '#7F7F7F', legendFontSize: 15 } ]
  
  if (!data || data.length === 0) {
    return <Text style={styles.noDataText}>Sem dados de região disponíveis.</Text>;
  }

  return (
    <View style={styles.chartContainer}>
      <Text style={styles.chartTitle}>Casos por Região</Text>
      <PieChart
        data={data}
        width={screenWidth - 40}
        height={220}
        chartConfig={chartConfig}
        accessor={"population"}
        backgroundColor={"transparent"}
        paddingLeft={"15"}
        center={[10, 0]}
        absolute
      />
    </View>
  );
};

export const AgeChart = ({ labels, values }) => {
  if (!labels || labels.length === 0) {
    return <Text style={styles.noDataText}>Sem dados de idade disponíveis.</Text>;
  }

  const data = {
    labels: labels,
    datasets: [
      {
        data: values
      }
    ]
  };

  return (
    <View style={styles.chartContainer}>
      <Text style={styles.chartTitle}>Casos por Faixa Etária</Text>
      <BarChart
        data={data}
        width={screenWidth - 40}
        height={250}
        yAxisLabel=""
        chartConfig={chartConfig}
        verticalLabelRotation={30}
        fromZero
        showValuesOnTopOfBars
      />
    </View>
  );
};

const styles = StyleSheet.create({
  chartContainer: {
    backgroundColor: Colors.white,
    borderRadius: 16,
    padding: 10,
    marginVertical: 10,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 3.84,
    elevation: 5,
  },
  chartTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: Colors.text,
    marginBottom: 10,
    marginTop: 5,
  },
  noDataText: {
    textAlign: 'center',
    color: Colors.lightGray,
    marginVertical: 20,
  }
});