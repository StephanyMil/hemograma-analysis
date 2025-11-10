import { useContext } from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { DrawerContentScrollView, DrawerItemList } from '@react-navigation/drawer';
import { MaterialIcons } from '@expo/vector-icons';
import { AuthContext } from '../context/AuthContext';
import Colors from '../constants/Colors';

export default function CustomDrawerContent(props) {
  const { logout } = useContext(AuthContext);

  return (
    <DrawerContentScrollView {...props} contentContainerStyle={{ flex: 1 }}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Menu</Text>
      </View>
      <DrawerItemList {...props} />
      <TouchableOpacity style={styles.logoutButton} onPress={logout}>
        <MaterialIcons name="logout" size={24} color={Colors.text} />
        <Text style={styles.logoutButtonText}>Sair</Text>
      </TouchableOpacity>
    </DrawerContentScrollView>
  );
}

const styles = StyleSheet.create({
  header: {
    padding: 20,
    borderBottomWidth: 1,
    borderBottomColor: Colors.lightGray,
  },
  headerTitle: {
    fontSize: 22,
    fontWeight: 'bold',
    color: Colors.text,
  },
  logoutButton: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 20,
    marginTop: 'auto',
    borderTopWidth: 1,
    borderTopColor: Colors.lightGray,
  },
  logoutButtonText: {
    marginLeft: 15,
    fontSize: 16,
    color: Colors.text,
    fontWeight: '500',
  },
});
