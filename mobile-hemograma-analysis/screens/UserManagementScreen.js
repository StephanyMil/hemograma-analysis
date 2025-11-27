import { useEffect, useState } from 'react';
import { View, Text, StyleSheet, SafeAreaView, TouchableOpacity, FlatList, Alert } from 'react-native';
import { MaterialIcons } from '@expo/vector-icons';
import Colors from '../constants/Colors';
import { getUsers, deleteUser } from '../api/apiService';
import { useNavigation } from '@react-navigation/native';

export default function UserManagementScreen() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigation = useNavigation();

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const res = await getUsers();
      setUsers(res.data);
    } catch (err) {
      console.error("Erro ao buscar usuários", err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = (userId) => {
    Alert.alert(
      "Confirmação",
      "Deseja realmente excluir este usuário?",
      [
        { text: "Cancelar", style: "cancel" },
        {
          text: "Excluir",
          style: "destructive",
          onPress: async () => {
            try {
              await deleteUser(userId);
              fetchUsers(); // atualiza a lista
            } catch (err) {
              console.error("Erro ao excluir usuário", err);
              Alert.alert("Erro", "Não foi possível excluir o usuário.");
            }
          },
        },
      ]
    );
  };

  const renderItem = ({ item }) => (
    <View style={styles.userItem}>
      <View style={{ flex: 1 }}>
        <Text style={styles.userName}>{item.name}</Text>
        <Text style={styles.userEmail}>{item.email}</Text>
      </View>

      <View style={styles.actions}>
        {/*
        <TouchableOpacity
          style={styles.actionButton}
          onPress={() => navigation.navigate('EditUser', { user: item })}
        >
          <MaterialIcons name="edit" size={24} color={Colors.primary} />
        </TouchableOpacity>
        */}

        <TouchableOpacity
          style={styles.actionButton}
          onPress={() => handleDelete(item.id)}
        >
          <MaterialIcons name="delete" size={24} color="red" />
        </TouchableOpacity>
      </View>
    </View>
  );

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.content}>
        {loading ? (
          <Text style={styles.emptyText}>Carregando usuários...</Text>
        ) : users.length === 0 ? (
          <Text style={styles.emptyText}>Nenhum usuário encontrado.</Text>
        ) : (
          <FlatList
            data={users}
            keyExtractor={(item) => item.id}
            renderItem={renderItem}
            contentContainerStyle={{ padding: 20 }}
          />
        )}
      </View>

      <TouchableOpacity
        style={styles.fab}
        onPress={() => navigation.navigate('CreateUser')}
      >
        <MaterialIcons name="add" size={30} color={Colors.white} />
      </TouchableOpacity>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  content: { flex: 1 },
  emptyText: { fontSize: 16, color: Colors.lightGray, textAlign: 'center', marginTop: 20 },
  userItem: {
    flexDirection: 'row',
    backgroundColor: Colors.white,
    padding: 15,
    borderRadius: 8,
    marginBottom: 10,
    borderWidth: 1,
    borderColor: '#ddd',
    alignItems: 'center',
  },
  userName: { fontSize: 16, fontWeight: 'bold', color: Colors.text },
  userEmail: { fontSize: 14, color: Colors.lightGray },
  actions: { flexDirection: 'row' },
  actionButton: { marginLeft: 10 },
  fab: {
    position: 'absolute',
    right: 30,
    bottom: 30,
    width: 60,
    height: 60,
    borderRadius: 30,
    backgroundColor: Colors.primary,
    justifyContent: 'center',
    alignItems: 'center',
    elevation: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.3,
    shadowRadius: 4,
  },
});
