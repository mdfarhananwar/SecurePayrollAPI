package account.model;

import account.repository.GroupRepository;
import account.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataLoader {

    private final GroupRepository groupRepository;
    private final RoleRepository roleRepository;


    @Autowired
    public DataLoader(GroupRepository groupRepository, RoleRepository roleRepository) {
        this.groupRepository = groupRepository;
        this.roleRepository = roleRepository;
        loadDataIfNotExists();
    }
    private void loadDataIfNotExists() {
        if (!groupRepository.existsByName("Administrative") && !groupRepository.existsByName("Business")) {
            createGroupAndRoles();
        }
    }

    private void createGroupAndRoles() {
        Group administrativeGroup = new Group("Administrative");
        Group businessGroup = new Group("Business");
        groupRepository.save(administrativeGroup);
        groupRepository.save(businessGroup);
        Role adminRole = new Role("ADMINISTRATOR");
        Role userRole = new Role("USER");
        Role accountantRole = new Role("ACCOUNTANT");
        roleRepository.save(adminRole);
        roleRepository.save(userRole);
        roleRepository.save(accountantRole);
    }
}
