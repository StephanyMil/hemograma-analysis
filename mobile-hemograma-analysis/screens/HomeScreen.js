import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, SafeAreaView, ScrollView } from 'react-native';
import { MaterialIcons } from '@expo/vector-icons';
import Colors from '../constants/Colors';
import GraphComponent from '../components/GraphComponent';

export default function HomeScreen({ navigation }) {
  // Dados simulados para o gráfico
  const chartData = {
    labels: ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun'],
    values: [5, 10, 8, 15, 12, 18],
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <ScrollView style={styles.container}>
        <View style={styles.filterSection}>
          <TouchableOpacity style={styles.filterButton}>
            <Text style={styles.filterButtonText}>Últimos 6 meses</Text>
            <MaterialIcons name="filter-list" size={20} color={Colors.text} />
          </TouchableOpacity>
        </View>

        <GraphComponent data={chartData} />

        <Text style={styles.sectionTitle}>Últimos Resultados</Text>
        <View style={styles.card}>
          <Text style={styles.cardText}>Resultado mais recente: Não Reagente</Text>
          <Text style={styles.cardDate}>Data: 20/08/2025</Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  container: {
    flex: 1,
    padding: 20,
  },
  filterSection: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    marginBottom: 20,
  },
  filterButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#E0E0E0',
    paddingVertical: 8,
    paddingHorizontal: 15,
    borderRadius: 20,
  },
  filterButtonText: {
    color: Colors.text,
    marginRight: 5,
    fontSize: 14,
    fontWeight: '500',
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: Colors.text,
    marginBottom: 15,
  },
  card: {
    backgroundColor: Colors.white,
    padding: 20,
    borderRadius: 12,
    marginBottom: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2.22,
    elevation: 3,
  },
  cardText: {
    fontSize: 16,
    color: Colors.text,
    fontWeight: '500',
  },
  cardDate: {
    fontSize: 12,
    color: Colors.lightGray,
    marginTop: 5,
  },
});