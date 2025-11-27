import { useFocusEffect } from '@react-navigation/native';
import { useCallback, useState } from 'react';
import { ActivityIndicator, RefreshControl, SafeAreaView, ScrollView, StyleSheet, Text, View } from 'react-native';
import { getEstatisticasPorIdade, getEstatisticasPorRegiao } from '../api/apiService';
import { AgeChart, RegionChart } from '../components/DashboardCharts';
import Colors from '../constants/Colors';

export default function StatisticsScreen() {
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [regionData, setRegionData] = useState([]);
  const [ageLabels, setAgeLabels] = useState([]);
  const [ageValues, setAgeValues] = useState([]);

  // Cores fixas para as regiões para manter consistência
  const regionColors = [
    '#FF6384', // Vermelho
    '#36A2EB', // Azul
    '#FFCE56', // Amarelo
    '#4BC0C0', // Verde Água
    '#9966FF', // Roxo
    '#FF9F40'  // Laranja
  ];

  const loadData = useCallback(async () => {
    try {
      // Carregar dados de Região
      const regionResponse = await getEstatisticasPorRegiao();
      const regionMap = regionResponse.data;
      
      // Transformar Map em Array para o PieChart
      const formattedRegionData = Object.keys(regionMap).map((key, index) => ({
        name: key,
        population: regionMap[key],
        color: regionColors[index % regionColors.length],
        legendFontColor: "#7F7F7F",
        legendFontSize: 15
      }));
      setRegionData(formattedRegionData);

      // Carregar dados de Idade
      const ageResponse = await getEstatisticasPorIdade();
      const ageMap = ageResponse.data;

      // Ordenar faixas etárias se necessário (assumindo formato "0-17", "18-29", etc)
      const sortedKeys = Object.keys(ageMap).sort(); 
      setAgeLabels(sortedKeys);
      setAgeValues(sortedKeys.map(key => ageMap[key]));

    } catch (error) {
      console.error('Erro ao carregar estatísticas', error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      loadData();
    }, [loadData])
  );

  const onRefresh = () => {
    setRefreshing(true);
    loadData();
  };

  if (loading) {
    return (
      <View style={styles.centerContainer}>
        <ActivityIndicator size="large" color={Colors.primary} />
      </View>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView 
        contentContainerStyle={styles.scrollContent}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={Colors.primary} />}
      >
        <Text style={styles.headerTitle}>Dashboard Analítico</Text>
        <Text style={styles.subHeader}>Estatísticas de casos detectados</Text>

        <RegionChart data={regionData} />
        <AgeChart labels={ageLabels} values={ageValues} />
        
        <View style={styles.footerSpace} />
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  centerContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: Colors.background,
  },
  scrollContent: {
    padding: 15,
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: Colors.primary,
    marginBottom: 5,
  },
  subHeader: {
    fontSize: 16,
    color: Colors.text,
    marginBottom: 20,
  },
  footerSpace: {
    height: 30,
  }
});