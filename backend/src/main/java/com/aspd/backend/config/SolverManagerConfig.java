package com.aspd.backend.config;

import com.aspd.backend.model.PlannedInternship;
import com.aspd.backend.model.StudentInternshipDemand;
import com.aspd.backend.solver.InternshipSolution;
import com.aspd.backend.solver.StudentAssignmentSolution;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.config.solver.SolverConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OptaPlanner SolverManager.
 * SolverManager enables async solving with better control over solver lifecycle.
 */
@Configuration
public class SolverManagerConfig {
    
    /**
     * SolverManager for Phase 1 (teacher assignment).
     * Allows async solving and early termination.
     */
    @Bean
    public SolverManager<InternshipSolution, String> phase1SolverManager() {
        SolverConfig solverConfig = SolverConfig.createFromXmlResource("solverConfig.xml");
        
        // Time limit will be overridden at runtime via withProblemId()
        return SolverManager.create(solverConfig, new org.optaplanner.core.config.solver.SolverManagerConfig());
    }
    
    /**
     * SolverManager for Phase 2 (student assignment).
     * Uses separate solver config for student assignment problem.
     */
    @Bean
    public SolverManager<StudentAssignmentSolution, String> phase2SolverManager() {
        SolverConfig solverConfig = SolverConfig.createFromXmlResource("studentAssignmentSolverConfig.xml");
        
        return SolverManager.create(solverConfig, new org.optaplanner.core.config.solver.SolverManagerConfig());
    }
}
