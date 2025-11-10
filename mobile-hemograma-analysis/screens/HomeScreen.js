import { useState, useCallback } from 'react';
import { SafeAreaView, FlatList, StyleSheet, Text, View, ActivityIndicator, RefreshControl, TouchableOpacity } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { getRecentHemogramas } from '../api/apiService';
import Colors from '../constants/Colors';

const HemogramaCard = ({ item }) => (
  <View style={styles.card}>
    <Text style={styles.cardTitle}>Exame de {new Date(item.data).toLocaleDateString()}</Text>
    <View style={styles.row}>
      <Text style={styles.itemText}>Leucócitos: <Text style={styles.valueText}>{item.leucocitos}</Text></Text>
      <Text style={styles.itemText}>Hemoglobina: <Text style={styles.valueText}>{item.hemoglobina}</Text></Text>
    </View>
    <View style={styles.row}>
      <Text style={styles.itemText}>Plaquetas: <Text style={styles.valueText}>{item.plaquetas}</Text></Text>
      <Text style={styles.itemText}>Hematócrito: <Text style={styles.valueText}>{item.hematocrito}%</Text></Text>
    </View>
  </View>
);

export default function HomeScreen() {
  const [hemogramas, setHemogramas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const loadData = useCallback(async () => {
    try {
      const response = await getRecentHemogramas();
      setHemogramas(response.data);
    } catch (error) {
      console.error('Falha ao carregar hemogramas', error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      setLoading(true);
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
      <FlatList
        data={hemogramas}
        renderItem={({ item }) => <HemogramaCard item={item} />}
        keyExtractor={(item) => item.id.toString()}
        ListHeaderComponent={<Text style={styles.header}>Hemogramas Recentes</Text>}
        ListEmptyComponent={<Text style={styles.emptyText}>Nenhum resultado encontrado.</Text>}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={Colors.primary} />}
        contentContainerStyle={{ padding: 15 }}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  centerContainer: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: Colors.background },
  header: { fontSize: 24, fontWeight: 'bold', color: Colors.text, marginBottom: 15, paddingHorizontal: 5 },
  emptyText: { textAlign: 'center', marginTop: 50, color: Colors.lightGray, fontSize: 16 },
  card: { backgroundColor: Colors.white, borderRadius: 8, padding: 15, marginVertical: 8, shadowColor: '#000', shadowOffset: { width: 0, height: 1 }, shadowOpacity: 0.1, shadowRadius: 3, elevation: 4 },
  cardTitle: { fontSize: 16, fontWeight: 'bold', color: Colors.primary, marginBottom: 10 },
  row: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 5 },
  itemText: { fontSize: 14, color: Colors.text },
  valueText: { fontWeight: 'bold' },
});
