import { MaterialIcons } from '@expo/vector-icons';
import { createDrawerNavigator } from '@react-navigation/drawer';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import { useContext, useEffect, useState } from 'react';
import { ActivityIndicator, View } from 'react-native';
import { isFirstUser } from '../api/apiService';
import Colors from '../constants/Colors';
import { AuthContext } from '../context/AuthContext';

import CustomDrawerContent from '../components/CustomDrawerContent';
import CreateUserScreen from '../screens/CreateUserScreen';
import HomeScreen from '../screens/HomeScreen';
import LoginScreen from '../screens/LoginScreen';
import RegisterFirstUserScreen from '../screens/RegisterFirstUserScreen';
import StatisticsScreen from '../screens/StatisticsScreen'; // <--- IMPORTE AQUI
import UserManagementScreen from '../screens/UserManagementScreen';

const Stack = createStackNavigator();
const Drawer = createDrawerNavigator();

function MainDrawerNavigator() {
  return (
    <Drawer.Navigator
      drawerContent={props => <CustomDrawerContent {...props} />}
      screenOptions={{
        headerStyle: { backgroundColor: Colors.background },
        headerTintColor: Colors.text,
        drawerActiveBackgroundColor: Colors.primary,
        drawerActiveTintColor: Colors.white,
      }}
    >
      <Drawer.Screen
        name="Dashboard"
        component={HomeScreen}
        options={{
          title: 'Hemogramas',
          drawerIcon: ({ color }) => <MaterialIcons name="list" size={24} color={color} />
        }}
      />
      {/* ADICIONE ESTA TELA */}
      <Drawer.Screen
        name="Statistics"
        component={StatisticsScreen}
        options={{
          title: 'Estatísticas',
          drawerIcon: ({ color }) => <MaterialIcons name="insert-chart" size={24} color={color} />
        }}
      />
      <Drawer.Screen
        name="UserManagement"
        component={UserManagementScreen}
        options={{
          title: 'Gerenciar Usuários',
          drawerIcon: ({ color }) => <MaterialIcons name="people" size={24} color={color} />
        }}
      />
    </Drawer.Navigator>
  );
}

// ... Resto do arquivo permanece igual ...
export default function AppNavigator() {
  const { userToken, isLoading: isAuthLoading } = useContext(AuthContext);
  const [isFirst, setIsFirst] = useState(false);
  const [isCheckingFirst, setIsCheckingFirst] = useState(true);

  useEffect(() => {
    const checkUserStatus = async () => {
      try {
        const response = await isFirstUser();
        setIsFirst(response.data.isFirstUser);
      } catch (error) {
        console.error("Não foi possível verificar o primeiro usuário.", error);
        setIsFirst(false);
      } finally {
        setIsCheckingFirst(false);
      }
    };
    checkUserStatus();
  }, []);

  if (isAuthLoading || isCheckingFirst) {
    return (
      <View style={{ flex: 1, justifyContent: 'center' }}>
        <ActivityIndicator size="large" color={Colors.primary} />
      </View>
    );
  }

  return (
    <NavigationContainer>
      <Stack.Navigator screenOptions={{ headerShown: false }}>
        {userToken == null ? (
          <>
            {isFirst ? (
              <Stack.Screen name="RegisterFirstUser" component={RegisterFirstUserScreen} />
            ) : (
              <Stack.Screen name="Login" component={LoginScreen} />
            )}
          </>
        ) : (
          <>
            <Stack.Screen name="Main" component={MainDrawerNavigator} />
            <Stack.Screen
              name="CreateUser"
              component={CreateUserScreen}
              options={{
                headerShown: true,
                title: 'Novo Usuário',
                headerStyle: { backgroundColor: Colors.background },
                headerTintColor: Colors.text,
              }}
            />
          </>
        )}
      </Stack.Navigator>
    </NavigationContainer>
  );
}