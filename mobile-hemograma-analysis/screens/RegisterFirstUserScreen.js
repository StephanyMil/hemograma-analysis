import { useState } from 'react';
import { Text, TextInput, TouchableOpacity, StyleSheet, SafeAreaView, ActivityIndicator, Alert, StatusBar } from 'react-native';
import { registerFirstUser } from '../api/apiService';
import Colors from '../constants/Colors';

export default function RegisterFirstUserScreen({ navigation }) {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleRegister = async () => {
    if (!name || !email || !password) {
      Alert.alert('Atenção', 'Todos os campos são obrigatórios.');
      return;
    }
    setLoading(true);
    try {
      await registerFirstUser({ name, email, password });
      Alert.alert('Sucesso!', 'Usuário administrador criado. Por favor, faça o login para continuar.');
      navigation.replace('Login');
    } catch (error) {
      Alert.alert('Erro', 'Não foi possível registrar o usuário. O sistema já pode ter um administrador.');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" />
      <Text style={styles.title}>Configuração Inicial</Text>
      <Text style={styles.subtitle}>Crie o primeiro usuário administrador do sistema.</Text>
      <TextInput
        style={styles.input}
        placeholder="Nome Completo"
        placeholderTextColor={Colors.lightGray}
        value={name}
        onChangeText={setName}
      />
      <TextInput
        style={styles.input}
        placeholder="E-mail"
        placeholderTextColor={Colors.lightGray}
        value={email}
        onChangeText={setEmail}
        keyboardType="email-address"
        autoCapitalize="none"
      />
      <TextInput
        style={styles.input}
        placeholder="Senha"
        placeholderTextColor={Colors.lightGray}
        value={password}
        onChangeText={setPassword}
        secureTextEntry
      />
      <TouchableOpacity style={styles.button} onPress={handleRegister} disabled={loading}>
        {loading ? <ActivityIndicator color={Colors.white} /> : <Text style={styles.buttonText}>Criar Administrador</Text>}
      </TouchableOpacity>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
    justifyContent: 'center',
    padding: 20,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: Colors.text,
    textAlign: 'center',
    marginBottom: 10,
  },
  subtitle: {
    fontSize: 16,
    color: Colors.lightGray,
    textAlign: 'center',
    marginBottom: 30,
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
