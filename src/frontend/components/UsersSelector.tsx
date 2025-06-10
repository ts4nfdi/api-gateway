import {UserResponse, userRestClient} from "@/app/api/UserRestClient";
import MultipleSelector, {SelectorOption} from "@/components/MultipleSelector";
import {useEffect, useState} from "react";


export default function UsersSelector({selected, onChange, placeholder}: any) {
    const [sourceOptions, setSourceOptions] = useState<SelectorOption[]>([]);
    const [selectedUsers, setSelectedUsers] = useState<SelectorOption[]>([]);
    const [loading, setLoading] = useState(false);
    useEffect(() => {
        if (selected) {
            const selectedOptions = selected.map((value: any) => {
                // Check if it exists in our loaded options
                const existingOption = sourceOptions.find(opt => opt.value === value);
                if (existingOption) return existingOption;

                // If not found but we have a value, create a temporary option
                return {label: value, value};
            });
            setSelectedUsers(selectedOptions);
        }
        setLoading(true);
        userRestClient.getAllUsers().then((x: any) => {
            const options = x.data.map((user: UserResponse) => ({
                label: user.username,
                value: user.username
            }));
            setSourceOptions(options);
        }).catch(err => {
            console.error("Failed to fetch users", err);
        }).finally(() => {
            setLoading(false);
        })
    }, []);

    const handleChanges = (e: SelectorOption[]) => {
        const values = e.map((o: any) => {
            // Check if it exists in our loaded options
            const existingOption = sourceOptions.find(opt => opt.value === o.value);
            if (existingOption) return existingOption;

            // If not found but we have a value, create a temporary option
            return {label: o.label, value: o.value};
        });
        setSelectedUsers(values);
        if (onChange) onChange(values.map((o: any) => o.value));
    }

    return (
        <div className="flex flex-col gap-2 w-full">
            {loading && <p>Loading...</p>}
            {!loading && sourceOptions.length === 0 && <p>No users found</p>}
            {!loading && sourceOptions.length > 0 &&
                <MultipleSelector
                    defaultOptions={sourceOptions}
                    value={selectedUsers}
                    placeholder={placeholder || "Select a user..."}
                    onChange={handleChanges}
                />
            }
        </div>
    );
}