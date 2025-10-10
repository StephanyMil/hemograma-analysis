import React from 'react';
import { View, Text, Button, StyleSheet } from 'react-native';

export default function LoginScreen({ navigation }) {
  const handleLogin = () => {
    navigation.navigate('Home'); 
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Tela de Login</Text>
      <Button
        title="Entrar no App"
        onPress={handleLogin}
        color="#D20709"
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5F5F5',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
    color: '#333333',
  },
});