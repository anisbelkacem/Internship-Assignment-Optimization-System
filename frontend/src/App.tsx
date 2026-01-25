import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./contexts/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import LoginPage from "./pages/LoginPage";
import Layout from "./components/Layout";
import Dashboard from "./pages/Dashboard";
import Students from "./pages/Students";
import Pls from "./pages/Pls";
import Schools from "./pages/Schools";
import InternshipAssignments from "./pages/InternshipAssignments";
import Assign from "./pages/Assign";
import Settings from "./pages/Settings";
import Courses from "./pages/Courses";
import AuditLogs from "./pages/AuditLogs";

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          
          {/* Protected routes */}
          <Route path="/" element={ <ProtectedRoute> <Layout /> </ProtectedRoute> }>
            <Route index element={<Dashboard />} />
            <Route path="dashboard" element={<Dashboard />} />
            <Route path="students" element={<Students />} />
            <Route path="pls" element={<Pls />} />
            <Route path="schools" element={<Schools />} />
            <Route path="assignments" element={<Assign />} />/*InternshipAssignments*/
            <Route path="assign" element={<InternshipAssignments />} />
            <Route path="settings" element={<Settings />} />
            <Route path="courses" element={<Courses />} />
            <Route path="audit-logs" element={<AuditLogs />} />
          </Route>
          
          {/* Catch all */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}