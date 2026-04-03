package com.example.task_management.application.usecases.impl.project;

import com.example.task_management.application.DTOUsecase.response.project.ProjectListResult;
import com.example.task_management.application.DTOUsecase.response.project.ProjectResult;
import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.ProjectRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.mapper.ProjectMapper;
import com.example.task_management.application.usecases.project.GetProjectListUseCase;
import com.example.task_management.domain.entities.ProjectMember;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.enums.InvitationStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GetProjectListUseCaseImpl implements GetProjectListUseCase {

        private final ProjectRepository projectRepository;
        private final ProjectMemberRepository projectMemberRepository;
        private final UserRepository userRepository;
        private final ProjectMapper projectMapper;

        public GetProjectListUseCaseImpl(
                        ProjectRepository projectRepository,
                        ProjectMemberRepository projectMemberRepository,
                        UserRepository userRepository,
                        ProjectMapper projectMapper) {
                this.projectRepository = projectRepository;
                this.projectMemberRepository = projectMemberRepository;
                this.userRepository = userRepository;
                this.projectMapper = projectMapper;
        }

        @Override
        public ProjectListResult getMyProjects(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Không tìm thấy người dùng hiện tại trong hệ thống."));

                List<ProjectMember> acceptedMemberships = projectMemberRepository
                                .findAllByUserIdAndInvitationStatus(user.getId(), InvitationStatus.ACCEPTED);

                List<ProjectResult> projects = acceptedMemberships.stream()
                                .map(membership -> projectRepository.findById(membership.getProjectId()).orElse(null))
                                .filter(Objects::nonNull)
                                .map(projectMapper::toProjectResponse)
                                .collect(Collectors.toList());

                return ProjectListResult.builder()
                                .projects(projects)
                                .totalProjects(projects.size())
                                .build();
        }

        @Override
        public ProjectListResult getProjectsByOwner(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Không tìm thấy người dùng hiện tại trong hệ thống."));

                List<ProjectResult> projects = projectRepository.findAllByOwnerId(user.getId()).stream()
                                .map(projectMapper::toProjectResponse)
                                .collect(Collectors.toList());

                return ProjectListResult.builder()
                                .projects(projects)
                                .totalProjects(projects.size())
                                .build();
        }
}
