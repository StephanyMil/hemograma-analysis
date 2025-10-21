import { useState } from 'react';
import { Text, TextInput, TouchableOpacity, StyleSheet, SafeAreaView, ActivityIndicator, Alert, StatusBar } from 'react-native';
import { createUser } from '../api/apiService';
import Colors from '../constants/Colors';

export default function CreateUserScreen({ navigation }) {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleCreate = async () => {
    if (!name || !email || !password) {
      Alert.alert('Atenção', 'Todos os campos são obrigatórios.');
      return;
    }
    setLoading(true);
    try {
      await createUser({ name, email, password });
      Alert.alert('Sucesso', 'Novo usuário criado com sucesso!');
      navigation.goBack();
    } catch (error) {
      Alert.alert('Erro', 'Não foi possível criar o usuário. Verifique se o e-mail já está em uso.');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" />
      <Text style={styles.title}>Cadastrar Novo Usuário</Text>
      <TextInput style={styles.input} placeholder="Nome Completo" value={name} onChangeText={setName} placeholderTextColor={Colors.lightGray} />
      <TextInput style={styles.input} placeholder="E-mail" value={email} onChangeText={setEmail} placeholderTextColor={Colors.lightGray} keyboardType="email-address" autoCapitalize="none" />
      <TextInput style={styles.input} placeholder="Senha Provisória" value={password} onChangeText={setPassword} placeholderTextColor={Colors.lightGray} secureTextEntry />
      <TouchableOpacity style={styles.button} onPress={handleCreate} disabled={loading}>
        {loading ? <ActivityIndicator color={Colors.white} /> : <Text style={styles.buttonText}>Salvar Usuário</Text>}
      </TouchableOpacity>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
    padding: 20,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: Colors.text,
    textAlign: 'center',
    marginBottom: 30,
    marginTop: 20,
  },
  input: {
    width: '100%',
    height: 50,
    backgroundColor: Colors.white,
    borderRadius: 8,
    paddingHorizontal: 15,
    fontSize: 16,
    borderWidth: 1,
    borderColor: '#ddd',
    marginBottom: 15,
  },
  button: {
    width: '100%',
    height: 50,
    backgroundColor: Colors.primary,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 10,
  },
  buttonText: {
    color: Colors.white,
    fontSize: 18,
    fontWeight: 'bold',
  },
});
