import 'react-native-gesture-handler';
import AppNavigator from './navigation/AppNavigator';
import { AuthProvider } from './context/AuthContext';

export default function App() {
  return (
    <AuthProvider>
      <AppNavigator />
    </AuthProvider>
  );
}
