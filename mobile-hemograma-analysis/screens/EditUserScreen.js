import { useState } from 'react';
import { Text, TextInput, TouchableOpacity, StyleSheet, SafeAreaView, ActivityIndicator, Alert, StatusBar } from 'react-native';
import Colors from '../constants/Colors';
import { updateUser } from '../api/apiService';

export default function EditUserScreen({ route, navigation }) {
  const { user } = route.params; // usuário passado da tela de gerenciamento
  const [name, setName] = useState(user.name);
  const [email, setEmail] = useState(user.email);
  const [loading, setLoading] = useState(false);

  const handleUpdate = async () => {
    if (!name || !email) {
      Alert.alert('Atenção', 'Todos os campos são obrigatórios.');
      return;
    }

    setLoading(true);
    try {
      await updateUser(user.id, { name, email });
      Alert.alert('Sucesso', 'Usuário atualizado com sucesso!');
      navigation.goBack();
    } catch (error) {
      console.error(error);
      Alert.alert('Erro', 'Não foi possível atualizar o usuário.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" />
      <Text style={styles.title}>Editar Usuário</Text>
      <TextInput
        style={styles.input}
        placeholder="Nome Completo"
        value={name}
        onChangeText={setName}
        placeholderTextColor={Colors.lightGray}
      />
      <TextInput
        style={styles.input}
        placeholder="E-mail"
        value={email}
        onChangeText={setEmail}
        placeholderTextColor={Colors.lightGray}
        keyboardType="email-address"
        autoCapitalize="none"
      />

      <TouchableOpacity style={styles.button} onPress={handleUpdate} disabled={loading}>
        {loading ? <ActivityIndicator color={Colors.white} /> : <Text style={styles.buttonText}>Salvar Alterações</Text>}
      </TouchableOpacity>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background, padding: 20 },
  title: { fontSize: 28, fontWeight: 'bold', color: Colors.text, textAlign: 'center', marginBottom: 30, marginTop: 20 },
  input: { width: '100%', height: 50, backgroundColor: Colors.white, borderRadius: 8, paddingHorizontal: 15, fontSize: 16, borderWidth: 1, borderColor: '#ddd', marginBottom: 15 },
  button: { width: '100%', height: 50, backgroundColor: Colors.primary, borderRadius: 8, alignItems: 'center', justifyContent: 'center', marginTop: 10 },
  buttonText: { color: Colors.white, fontSize: 18, fontWeight: 'bold' },
});
