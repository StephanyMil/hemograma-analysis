import { useContext, useState, useEffect } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import { createDrawerNavigator } from '@react-navigation/drawer';
import { View, ActivityIndicator } from 'react-native';
import { MaterialIcons } from '@expo/vector-icons';
import { AuthContext } from '../context/AuthContext';
import { isFirstUser } from '../api/apiService';
import Colors from '../constants/Colors';

import LoginScreen from '../screens/LoginScreen';
import RegisterFirstUserScreen from '../screens/RegisterFirstUserScreen';
import HomeScreen from '../screens/HomeScreen';
import UserManagementScreen from '../screens/UserManagementScreen';
import CreateUserScreen from '../screens/CreateUserScreen';
import CustomDrawerContent from '../components/CustomDrawerContent';

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
          title: 'Dashboard de Hemogramas',
          drawerIcon: ({ color }) => <MaterialIcons name="dashboard" size={24} color={color} />
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
